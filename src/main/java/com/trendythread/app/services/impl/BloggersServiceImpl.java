package com.trendythread.app.services.impl;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.services.BloggersService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BloggersServiceImpl implements BloggersService {

    @Autowired
    private BloggersRepository bloggersRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BloggerDto createBlogger(BloggerDto bloggerDto) {
        log.info("createBlogger - request received: BloggerDto={}", bloggerDto);
        Blogger blogger = this.dtoToBlogger(bloggerDto);
        Blogger savedBlogger = this.bloggersRepository.save(blogger);
        BloggerDto result = this.BloggerToDto(savedBlogger);
        log.info("createBlogger - Blogger created: id={}", result.getId());
        return result;
    }

    @Override
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
        log.info("updateByBloggerId - update successful: id={}", id);
        return result;
    }

    @Override
    public BloggerDto findByBloggerId(Integer BloggerId) {
        log.info("findByBloggerId - request received: id={}", BloggerId);
        Blogger blogger = this.bloggersRepository.findById(BloggerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "id", BloggerId));

        BloggerDto result = this.BloggerToDto(blogger);
        log.debug("findByBloggerId - fetched Blogger: {}", result);
        return result;
    }

    @Override
    public List<BloggerDto> fetchAllBloggers() {
        log.info("fetchAllBloggers - request received");
        List<Blogger> listOfBloggers = this.bloggersRepository.findAll();
        List<BloggerDto> dtoList = listOfBloggers.stream().map(this::BloggerToDto).toList();
        log.debug("fetchAllBloggers - fetched {} Bloggers", dtoList.size());
        return dtoList;
    }

    @Override
    public void deleteByBloggerId(Integer id) {
        log.info("fetchByBloggerId (delete) - request received: id={}", id);
        Blogger blogger = this.bloggersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Id", id));
        this.bloggersRepository.delete(blogger);
        log.info("fetchByBloggerId (delete) - deleted Blogger id={}", id);
    }

    @Override
    public BloggerDto findByEmail(String email) {
        log.debug("findByEmail - request received: email={}", email);
        Optional<Blogger> blogger = this.bloggersRepository.findByEmail(email);
        if (blogger.isPresent()) {
            log.debug("findByEmail - Blogger found: id={}", blogger.get().getId());
            return this.BloggerToDto(blogger.get());
        }
        log.debug("findByEmail - no Blogger found with email: {}", email);
        return null;
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
