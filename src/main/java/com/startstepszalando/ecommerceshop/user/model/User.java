package com.startstepszalando.ecommerceshop.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "users")
@ToString
@EqualsAndHashCode
public class User implements UserDetails {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

    private String name;
    private String email;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User(Long id, String email, String password, List<GrantedAuthority> authorities) {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static User build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

        return new User(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities);
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
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
}
