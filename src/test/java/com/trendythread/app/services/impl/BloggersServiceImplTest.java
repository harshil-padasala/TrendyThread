package com.trendythread.app.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.dto.UpdateProfileDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.util.RedisCacheSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloggersServiceImplTest {

    @Mock
    private BloggersRepository bloggersRepository;

    @Mock
    private PasswordEncoder bCryptPasswordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RedisCacheSupport redisCacheSupport;

    @Mock
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void createBloggerDoesNotTouchRedisBeforeCommitThenPopulatesIdEmailAndEvictsAllCacheAfterCommit() {
        BloggersServiceImpl service = new BloggersServiceImpl(bloggersRepository, bCryptPasswordEncoder, modelMapper, redisCacheSupport, objectMapper);
        BloggerDto request = bloggerDto(0, "new@example.com");
        request.setPassword("rawpass");
        Blogger transientBlogger = blogger(0, "new@example.com");
        Blogger savedBlogger = blogger(1, "new@example.com");
        BloggerDto savedDto = bloggerDto(1, "new@example.com");

        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encoded");
        when(modelMapper.map(eq(request), eq(Blogger.class))).thenReturn(transientBlogger);
        when(bloggersRepository.save(transientBlogger)).thenReturn(savedBlogger);
        when(modelMapper.map(savedBlogger, BloggerDto.class)).thenReturn(savedDto);

        TransactionSynchronizationManager.initSynchronization();

        BloggerDto result = service.createBlogger(request);

        assertEquals(1, result.getId());
        verify(redisCacheSupport, never()).set(anyString(), any(), any());
        verify(redisCacheSupport, never()).delete(anyString());

        triggerAfterCommit();

        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_ID.concat("1")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_EMAIL.concat("new@example.com")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_BLOGGERS_CACHE);
    }

    @Test
    void updateByBloggerIdRefreshesIdAndEmailCachesAndEvictsAllCacheAfterCommit() {
        BloggersServiceImpl service = new BloggersServiceImpl(bloggersRepository, bCryptPasswordEncoder, modelMapper, redisCacheSupport, objectMapper);
        BloggerDto request = bloggerDto(2, "updated@example.com");
        request.setPassword("rawpass");
        Blogger existingBlogger = blogger(2, "old@example.com");
        BloggerDto savedDto = bloggerDto(2, "updated@example.com");

        when(bloggersRepository.findById(2)).thenReturn(Optional.of(existingBlogger));
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encoded");
        when(modelMapper.map(existingBlogger, BloggerDto.class)).thenReturn(savedDto);

        TransactionSynchronizationManager.initSynchronization();

        service.updateByBloggerId(request, 2);

        verify(redisCacheSupport, never()).set(anyString(), any(), any());
        verify(redisCacheSupport, never()).delete(anyString());

        triggerAfterCommit();

        verify(bloggersRepository).save(existingBlogger);
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_ID.concat("2")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_EMAIL.concat("updated@example.com")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_BLOGGERS_CACHE);
    }

    @Test
    void updateCurrentUserProfileRefreshesIdAndEmailCachesAfterCommit() {
        BloggersServiceImpl service = new BloggersServiceImpl(bloggersRepository, bCryptPasswordEncoder, modelMapper, redisCacheSupport, objectMapper);
        UpdateProfileDto updateProfileDto = new UpdateProfileDto();
        updateProfileDto.setFirstName("Jane");
        updateProfileDto.setLastName("Doe");
        updateProfileDto.setAbout("Bio");
        Blogger existingBlogger = blogger(3, "profile@example.com");
        BloggerDto savedDto = bloggerDto(3, "profile@example.com");

        when(bloggersRepository.findByEmail("profile@example.com")).thenReturn(Optional.of(existingBlogger));
        when(bloggersRepository.save(existingBlogger)).thenReturn(existingBlogger);
        when(modelMapper.map(existingBlogger, BloggerDto.class)).thenReturn(savedDto);

        TransactionSynchronizationManager.initSynchronization();

        service.updateCurrentUserProfile(updateProfileDto, "profile@example.com");
        triggerAfterCommit();

        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_ID.concat("3")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_BLOGGER_EMAIL.concat("profile@example.com")), eq(savedDto), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_BLOGGERS_CACHE);
    }

    @Test
    void deleteByBloggerIdEvictsIdEmailAndAllCachesAfterCommit() {
        BloggersServiceImpl service = new BloggersServiceImpl(bloggersRepository, bCryptPasswordEncoder, modelMapper, redisCacheSupport, objectMapper);
        Blogger existingBlogger = blogger(4, "deleted@example.com");

        when(bloggersRepository.findById(4)).thenReturn(Optional.of(existingBlogger));

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByBloggerId(4);

        verify(bloggersRepository).delete(existingBlogger);
        verify(redisCacheSupport, never()).delete(anyString());

        triggerAfterCommit();

        verify(redisCacheSupport).delete(IRedisConstant.REDIS_BLOGGER_ID.concat("4"));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_BLOGGER_EMAIL.concat("deleted@example.com"));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_BLOGGERS_CACHE);
    }

    @Test
    void findByBloggerIdReturnsCachedBloggerWithoutHittingRepository() {
        BloggersServiceImpl service = new BloggersServiceImpl(bloggersRepository, bCryptPasswordEncoder, modelMapper, redisCacheSupport, objectMapper);
        BloggerDto cachedBlogger = bloggerDto(5, "cached@example.com");

        when(redisCacheSupport.get(IRedisConstant.REDIS_BLOGGER_ID.concat("5"))).thenReturn(cachedBlogger);

        BloggerDto result = service.findByBloggerId(5);

        assertEquals(5, result.getId());
        verify(bloggersRepository, never()).findById(5);
    }

    private void triggerAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private BloggerDto bloggerDto(int id, String email) {
        BloggerDto dto = new BloggerDto();
        dto.setId(id);
        dto.setEmail(email);
        dto.setUserName("user" + id);
        dto.setFirstName("Test");
        dto.setLastName("User");
        return dto;
    }

    private Blogger blogger(int id, String email) {
        Blogger blogger = new Blogger();
        blogger.setId(id);
        blogger.setEmail(email);
        blogger.setUserName("user" + id);
        blogger.setFirstName("Test");
        blogger.setLastName("User");
        return blogger;
    }
}
