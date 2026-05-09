package com.trendythread.app.services.impl;

import com.trendythread.app.dto.BloggerDto;
import com.trendythread.app.exceptions.EmailSendingException;
import com.trendythread.app.services.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(BloggerDto bloggerDto) {
        try {

            // Prepare the email content using Thymeleaf template
            Context context = new Context();
            context.setVariable("username", bloggerDto.getUserName());
            context.setVariable("firstName", bloggerDto.getFirstName());
            context.setVariable("lastName", bloggerDto.getLastName());

            // Process the template with the provided context
            String welcomeEmailContent = templateEngine.process("email-template/welcome-email", context);

            // Create the mail
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper =
                    new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            mimeMessageHelper.setTo(bloggerDto.getEmail());
            mimeMessageHelper.setSubject("Welcome to TrendyThread!");
            mimeMessageHelper.setText(welcomeEmailContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
            log.info("Welcome email sent to: {}", bloggerDto.getEmail());

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", bloggerDto.getEmail(), e.getMessage());
            throw new EmailSendingException("Failed to send welcome email. Please try again later.", e);
        }

    }
}
