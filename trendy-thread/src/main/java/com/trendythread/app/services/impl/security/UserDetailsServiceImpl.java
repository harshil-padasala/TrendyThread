package com.trendythread.app.services.impl.security;

import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.BloggerPrincipal;
import com.trendythread.app.repositories.BloggersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final BloggersRepository bloggersRepository;

    @Autowired
    public UserDetailsServiceImpl(BloggersRepository bloggersRepository) {
        this.bloggersRepository = bloggersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Username parameter contains email address (as configured in AuthController)
        Optional<Blogger> blogger = bloggersRepository.findByEmail(username);

        if (blogger.isEmpty()) {
            log.error("UserDetailsServiceImpl - loadUserByUsername: User not found with email={}", username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return new BloggerPrincipal(blogger.get());
    }
}
