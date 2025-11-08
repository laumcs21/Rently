package com.Rently.Configuration.Security;

import com.Rently.Persistence.Entity.Persona;
import com.Rently.Persistence.Entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24h por defecto
    private long expirationMs;

    // =======================
    // Extracciones
    // =======================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // "sub" = email
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> (String) c.get("role"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // =======================
    // Generación de tokens
    // =======================

    /** Firma token para cualquier Persona (Usuario/Anfitrión/Admin) */
    public String generateToken(Persona persona) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", persona.getRol().name());
        return generateToken(claims, persona.getEmail());
    }

    /** (Opcional) Mantén compatibilidad: delega al de Persona */
    public String generateToken(Usuario usuario) {
        return generateToken((Persona) usuario);
    }

    /** (Opcional) por si usas UserDetails en algún flujo */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateToken(extraClaims, userDetails.getUsername());
    }

    // Core
    private String generateToken(Map<String, Object> extraClaims, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getSignInKey()) // jjwt 0.12 infiere HS según la key
                .compact();
    }

    // =======================
    // Validación
    // =======================

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =======================
    // Internos
    // =======================

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())   // jjwt 0.12
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
