package spring.tripmate.security;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import spring.tripmate.config.JwtProperties;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key secretKey;
    private final long expiration = 1000 * 60 * 60 * 2; // 2시간

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("❌ JWT secret key is missing. Check your environment or application.yml");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        System.out.println("✅ JwtProvider initialized with secretKey.");
    }

    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token); // 예외 발생 없으면 유효
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(300) // 5분 여유
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("토큰이 만료되었습니다. 다시 로그인 해주세요.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("JWT 파싱 실패", e);
        }
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        User principal = new User(email, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}
