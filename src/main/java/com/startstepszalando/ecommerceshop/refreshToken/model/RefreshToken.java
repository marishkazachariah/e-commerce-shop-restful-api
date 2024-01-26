package com.startstepszalando.ecommerceshop.refreshToken.model;

import com.startstepszalando.ecommerceshop.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "token")
        })
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;

    @NotBlank
    @NotNull
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}