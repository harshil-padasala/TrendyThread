package com.trendythread.app.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class BloggerPrincipal implements UserDetails {

    private final Blogger blogger;

    public BloggerPrincipal(Blogger blogger) {
        this.blogger = blogger;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return the actual role from the Blogger entity
        String role = blogger.getRole();

        // Ensure role has ROLE_ prefix (Spring Security convention)
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        // Default to ROLE_USER if no role is set
        if (role == null) {
            role = "ROLE_USER";
        }

        String finalRole = role;
        return Collections.singleton(() -> finalRole);
    }

    @Override
    public String getPassword() {
        return blogger.getPassword();
    }

    @Override
    public String getUsername() {
        // Return email as username since authentication is based on email
        return blogger.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
