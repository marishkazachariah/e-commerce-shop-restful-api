package com.startstepszalando.ecommerceshop.user.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.startstepszalando.ecommerceshop.user.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    @JsonIgnore
    private String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserImpl(Long id, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

        return new UserImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public long getId(){
        return id;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserImpl user = (UserImpl) o;
        return Objects.equals(id, user.id);
    }
}