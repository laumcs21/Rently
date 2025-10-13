package com.Rently.Business.Integration;

import com.Rently.Configuration.Security.JwtService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    public JwtService jwtService() {
        JwtService jwtService = Mockito.mock(JwtService.class);

        Mockito.when(jwtService.extractUsername(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        return SecurityContextHolder.getContext().getAuthentication().getName();
                    }
                    return "mockuser@test.com";
                });

        return jwtService;
    }
}
