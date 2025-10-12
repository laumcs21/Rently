package com.Rently.Business.Service.Util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SimpleCodeGenerator implements CodeGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate6Digits() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
