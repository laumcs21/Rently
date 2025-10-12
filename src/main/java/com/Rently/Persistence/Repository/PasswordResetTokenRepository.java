package com.Rently.Persistence.Repository;

import com.Rently.Persistence.Entity.PasswordResetToken;
import com.Rently.Persistence.Entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByPersonaId(Long personaId);
    void deleteByPersonaId(Long personaId);
    Optional<PasswordResetToken>  findTopByPersonaAndUsedFalseOrderByCreatedAtDesc (Persona persona);
}

