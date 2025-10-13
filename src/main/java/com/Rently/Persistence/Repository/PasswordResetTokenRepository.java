package com.Rently.Persistence.Repository;

import com.Rently.Persistence.Entity.PasswordResetToken;
import com.Rently.Persistence.Entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByPersonaId(Long personaId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByPersonaId(Long personaId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByPersona(Persona persona);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByPersonaAndUsedFalse(Persona persona);

    Optional<PasswordResetToken>  findTopByPersonaAndUsedFalseOrderByCreatedAtDesc (Persona persona);
}

