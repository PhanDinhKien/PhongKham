/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.security;

import mirai.sale.modules.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTokenMinutes; 
    
    public JwtService(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes) {
       this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
       this.accessTokenMinutes = accessTokenMinutes;
    }
    
    public String generateAccessToken(User user, List<String> roles) {
         Instant now = Instant.now();
         return Jwts.builder().subject(user.getId().toString())
                 .claim("email", user.getEmail())
                 .claim("roles", roles)
                 .issuedAt(Date.from(now))
                 .expiration(Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES)))
                 .signWith(key)
                 .compact(); 
                 
    }
    
    public Claims parse(String token) {
      return  Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); 
    }

    public long getAccessTokenSeconds() {
        return accessTokenMinutes * 60;
    }
}
