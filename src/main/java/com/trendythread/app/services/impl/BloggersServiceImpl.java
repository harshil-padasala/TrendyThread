package com.trendythread.app.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.dto.UpdateProfileDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.services.BloggersService;
import com.trendythread.app.util.RedisCacheSupport;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BloggersServiceImpl implements BloggersService {

    private final BloggersRepository bloggersRepository;

    private final PasswordEncoder bCryptPasswordEncoder;

    private final ModelMapper modelMapper;

    private final RedisCacheSupport redisCacheSupport;

    private final ObjectMapper objectMapper;

    @Autowired
	public BloggersServiceImpl(BloggersRepository bloggersRepository, PasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper, RedisCacheSupport redisCacheSupport, ObjectMapper objectMapper) {
		this.bloggersRepository = bloggersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.modelMapper = modelMapper;
		this.redisCacheSupport = redisCacheSupport;
		this.objectMapper = objectMapper;
	}

    @Override
    @Transactional
    public BloggerDto createBlogger(BloggerDto bloggerDto) {
        log.info("createBlogger - request received: BloggerDto={}", bloggerDto);
        Blogger blogger = this.dtoToBlogger(bloggerDto);
        Blogger savedBlogger = this.bloggersRepository.save(blogger);
        BloggerDto result = this.BloggerToDto(savedBlogger);

        scheduleAfterCommit(() -> {
            updateBloggerCacheByEmailAndID(result);
            evictAllBloggersCache();
        });

        log.info("createBlogger - Blogger created: id={}", result.getId());
        return result;
    }

    @Override
    @Transactional
    public BloggerDto updateByBloggerId(BloggerDto bloggerDto, Integer id) {
        log.info("updateByBloggerId - request received: id={}, BloggerDto={}", id, bloggerDto);
        Blogger updatedBlogger = this.bloggersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "id", id));
        updatedBlogger.setUserName(bloggerDto.getUserName());
        updatedBlogger.setEmail(bloggerDto.getEmail());
        updatedBlogger.setFirstName(bloggerDto.getFirstName());
        updatedBlogger.setLastName(bloggerDto.getLastName());
        updatedBlogger.setPassword(bCryptPasswordEncoder.encode(bloggerDto.getPassword()));
        updatedBlogger.setPlainPassword(bloggerDto.getPassword());
        updatedBlogger.setAbout(bloggerDto.getAbout());

        this.bloggersRepository.save(updatedBlogger);

        BloggerDto result = this.BloggerToDto(updatedBlogger);

        scheduleAfterCommit(() -> {
            updateBloggerCacheByEmailAndID(result);
            evictAllBloggersCache();
        });

        log.info("updateByBloggerId - update successful: id={}", id);
        return result;
    }

    @Override
    public BloggerDto findByBloggerId(Integer BloggerId) {
        log.info("findByBloggerId - request received: id={}", BloggerId);

        String cacheKey = IRedisConstant.REDIS_BLOGGER_ID.concat(String.valueOf(BloggerId));
        // Check Redis cache first
         Object cachedBlogger = redisCacheSupport.get(cacheKey);
         if (Objects.nonNull(cachedBlogger)) {
             log.debug("findByBloggerId - Blogger found in cache: id={}", BloggerId);
             return (BloggerDto) cachedBlogger;
         }

        Blogger blogger = this.bloggersRepository.findById(BloggerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "id", BloggerId));

        BloggerDto result = this.BloggerToDto(blogger);
        // Store in Redis cache for future requests
        redisCacheSupport.set(cacheKey, result, IRedisTtlConstant.TTL_ENTITY);
        log.debug("findByBloggerId - fetched Blogger: {}", result);
        return result;
    }

    @Override
    public List<BloggerDto> fetchAllBloggers() {
        log.info("fetchAllBloggers - request received");

        String cacheKey = IRedisConstant.REDIS_ALL_BLOGGERS_CACHE;

        // Check Redis cache first
        Object bloggers = redisCacheSupport.get(cacheKey);
        if (Objects.nonNull(bloggers)) {
            log.debug("fetchAllBloggers - Bloggers found in cache, count={}", ((List<?>) bloggers).size());
            return objectMapper.convertValue(bloggers, new  TypeReference<List<BloggerDto>>(){});
        }

        List<Blogger> listOfBloggers = this.bloggersRepository.findAll();
        List<BloggerDto> dtoList = listOfBloggers.stream().map(this::BloggerToDto).toList();

        // Store in Redis cache for future requests
        redisCacheSupport.set(cacheKey, dtoList, IRedisTtlConstant.TTL_COLLECTION);

        log.debug("fetchAllBloggers - fetched {} Bloggers", dtoList.size());
        return dtoList;
    }

    @Override
    @Transactional
    public void deleteByBloggerId(Integer id) {
        log.info("fetchByBloggerId (delete) - request received: id={}", id);
        Blogger blogger = this.bloggersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Id", id));
        String email = blogger.getEmail();
        this.bloggersRepository.delete(blogger);

        scheduleAfterCommit(() -> {
            redisCacheSupport.delete(IRedisConstant.REDIS_BLOGGER_ID.concat(String.valueOf(id)));
            redisCacheSupport.delete(IRedisConstant.REDIS_BLOGGER_EMAIL.concat(email));
            evictAllBloggersCache();
        });

        log.info("fetchByBloggerId (delete) - deleted Blogger id={}", id);
    }

    @Override
    public BloggerDto findByEmail(String email) {
        log.debug("findByEmail - request received: email={}", email);

        String cacheKey = IRedisConstant.REDIS_BLOGGER_EMAIL.concat(email);
        // Check Redis cache first
        Object cachedBlogger = redisCacheSupport.get(cacheKey);
        if (Objects.nonNull(cachedBlogger)) {
            log.debug("findByEmail - Blogger found in cache: email={}", email);
            return (BloggerDto) cachedBlogger;
        }

        Optional<Blogger> blogger = this.bloggersRepository.findByEmail(email);
        if (blogger.isPresent()) {
            log.debug("findByEmail - Blogger found: id={}", blogger.get().getId());
            BloggerDto bloggerDto = this.BloggerToDto(blogger.get());

            // Store in redis cache for future
            redisCacheSupport.set(cacheKey, bloggerDto, IRedisTtlConstant.TTL_ENTITY);

            return bloggerDto;
        }
        log.debug("findByEmail - no Blogger found with email: {}", email);
        return null;
    }

    @Override
    @Transactional
    public BloggerDto updateCurrentUserProfile(UpdateProfileDto updateProfileDto, String authenticatedUserEmail) {
        log.info("updateCurrentUserProfile - request received: email={}, updateData={}", authenticatedUserEmail, updateProfileDto);
        
        // Find the blogger by email
        Blogger blogger = this.bloggersRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "email", authenticatedUserEmail));
        
        // Update only the profile fields (no password change)
        blogger.setFirstName(updateProfileDto.getFirstName());
        blogger.setLastName(updateProfileDto.getLastName());
        blogger.setAbout(updateProfileDto.getAbout());
        
        Blogger savedBlogger = this.bloggersRepository.save(blogger);
        BloggerDto result = this.BloggerToDto(savedBlogger);

        scheduleAfterCommit(() -> {
            updateBloggerCacheByEmailAndID(result);
            evictAllBloggersCache();
        });

        log.info("updateCurrentUserProfile - update successful: id={}", result.getId());
        return result;
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private void updateBloggerCacheByEmailAndID(BloggerDto result) {
        String emailCacheKey  = IRedisConstant.REDIS_BLOGGER_EMAIL.concat(String.valueOf(result.getEmail()));
        String idCacheKey = IRedisConstant.REDIS_BLOGGER_ID.concat(String.valueOf(result.getId()));
        // Refresh Redis cache for both email and id keys
        redisCacheSupport.set(idCacheKey, result, IRedisTtlConstant.TTL_ENTITY);
        redisCacheSupport.set(emailCacheKey, result, IRedisTtlConstant.TTL_ENTITY);
    }

    private void evictAllBloggersCache() {
        redisCacheSupport.delete(IRedisConstant.REDIS_ALL_BLOGGERS_CACHE);
        log.debug("evictAllBloggersCache - deleted aggregate bloggers cache key");
    }

    private Blogger dtoToBlogger(BloggerDto bloggerDto) {
        String password = bloggerDto.getPassword();
        bloggerDto.setPassword(bCryptPasswordEncoder.encode(bloggerDto.getPassword()));
        Blogger blogger = this.modelMapper.map(bloggerDto, Blogger.class);
        blogger.setPlainPassword(password);
        return blogger;
    }

    private BloggerDto BloggerToDto(Blogger blogger) {
        return this.modelMapper.map(blogger, BloggerDto.class);
    }

}
