package com.trendythread.app.services;

import com.trendythread.app.dto.BloggerDto;

public interface EmailService {

    public void sendWelcomeEmail(BloggerDto bloggerDto);
}
