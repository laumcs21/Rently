package com.Rently.Business.DTO.Auth;

import com.Rently.Persistence.Entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
        private String token;
        private String nombre;
        private Rol rol;
    }
