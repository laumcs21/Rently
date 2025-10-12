package com.Rently.Business.Service.impl;

import com.Rently.Business.Service.EmailService;
import com.Rently.Business.Service.PasswordResetService;
import com.Rently.Business.Service.Util.SimpleCodeGenerator;
import com.Rently.Persistence.Entity.Persona;
import com.Rently.Persistence.Repository.PasswordResetTokenRepository;
import com.Rently.Persistence.Repository.PersonaRepository;
import com.Rently.Persistence.Entity.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

 @Service
    @RequiredArgsConstructor
    @Slf4j
    public class PasswordResetServiceImpl implements PasswordResetService {

        private final PersonaRepository personaRepository;
        private final PasswordResetTokenRepository tokenRepository;
        private final EmailService mailService;
        private final SimpleCodeGenerator codeGenerator;
        private final PasswordEncoder passwordEncoder;
        private static final SecureRandom RANDOM = new SecureRandom();
        private static final int EXP_MINUTES = 10;
     private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

     @Override
     public void requestCode(String email) {
         personaRepository.findByEmail(email).ifPresentOrElse(persona -> {
             String code = codeGenerator.generate6Digits();
             saveCode(persona, code);
             log.info("[RESET] Token creado para {}: {}", email, code);
             mailService.send(email, code);
             log.info("[RESET] Correo de recuperación enviado a {}", email);
         }, () -> {
             log.info("[RESET] Email no registrado: {} (respondemos 200 para no filtrar información)", email);
         });
     }
     public void resetPassword(String email, String code, String newPassword) {
         Persona persona = personaRepository.findByEmail(email)
                 .orElseThrow(() -> new IllegalArgumentException("Código inválido"));
         PasswordResetToken t = tokenRepository.findTopByPersonaAndUsedFalseOrderByCreatedAtDesc(persona)
                 .orElseThrow(() -> new IllegalArgumentException("Código inválido"));
         if (!t.getCode().equals(code) || t.getExpiresAt().isBefore(Instant.now())) {
             throw new IllegalArgumentException("Código inválido");
         }
         persona.setContrasena(passwordEncoder.encode(newPassword));
         personaRepository.save(persona);
         t.setUsed(true);
         tokenRepository.save(t);
     }

     private void saveCode(Persona persona, String code) {
         tokenRepository.deleteByPersonaId(persona.getId());

         PasswordResetToken token = PasswordResetToken.builder()
                 .persona(persona)
                 .code(code)
                 .createdAt(Instant.now())
                 .expiresAt(Instant.now().plus(EXP_MINUTES, ChronoUnit.MINUTES))
                 .used(false)
                 .build();

         tokenRepository.save(token);
     }

 }


