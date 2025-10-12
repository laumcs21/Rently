package com.Rently.Persistence.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_password_reset_persona", columnNames = "persona_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false, length = 10)
    private String code; // ej. 6 dígitos

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Boolean used;

    @Column(name="created_at", nullable=false,
            columnDefinition="DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private Instant createdAt;


    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}