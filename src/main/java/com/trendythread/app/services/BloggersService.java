package com.trendythread.app.services;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.entities.Blogger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BloggersService {

    BloggerDto createBlogger(BloggerDto bloggerDto);

    BloggerDto updateByBloggerId(BloggerDto bloggerDto, Integer id);

    BloggerDto findByBloggerId(Integer bloggerId);

    List<BloggerDto> fetchAllBloggers();

    void deleteByBloggerId(Integer id);

    BloggerDto findByEmail(String email);
}
