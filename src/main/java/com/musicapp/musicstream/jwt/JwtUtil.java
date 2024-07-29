package com.musicapp.musicstream.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.musicapp.musicstream.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Autowired
    private UserRepository userRepository;

    public JwtUtil() {
        // Decodifica la clave secreta de Base64
        String base64SecretKey = "SGF6dGVVbmFHYXlvbGFNaUN1Y29KZWplUXVlTWlyYXNBcXVpRmFu"; 
        byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    
    // Obtener todos los roles del usuario y agregarlos al token
    List<String> roles = userRepository.findByUsername(username).getRoles().stream()
            .map(role -> "ROLE_" + role.getName()) // Asegúrate de que role.getName() sea el nombre del rol
            .collect(Collectors.toList());

    claims.put("roles", roles);
    
    return createToken(claims, username);
}

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Expira en 10 horas
                .signWith(secretKey) // Usa la clave decodificada
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String usernameToken = extractUsername(token);
        // Compara el nombre de usuario del token con el nombre de usuario del UserDetails
        userDetails.getUsername();
        usernameToken.equals(userDetails.getUsername());
        return (usernameToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
