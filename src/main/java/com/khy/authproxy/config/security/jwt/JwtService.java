package com.khy.authproxy.config.security.jwt;

import com.khy.authproxy.domain.member.entity.Member;
import com.khy.authproxy.domain.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.ACCESS_TOKEN_VALID_TIME}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${jwt.REFRESH_TOKEN_VALID_TIME}")
    private long REFRESH_TOKEN_EXPIRATION;

    private final MemberRepository memberRepository;

    public String createAccessToken(String refreshToken) {

        // Refresh token validation can be added here if needed
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        if(!validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userName = getUserNameFromToken(refreshToken);

        if(!memberRepository.findFirstByLoginId(userName).getToken().equals(refreshToken)){
            throw new IllegalArgumentException("Refresh token does not match the user's stored token");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        var key = Keys.hmacShaKeyFor(keyBytes);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(userName)
                .issuer("authproxy")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    @Transactional
    public String createRefreshToken(String userName) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        var key = Keys.hmacShaKeyFor(keyBytes);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        String refreshToken = Jwts.builder()
                .subject(userName)
                .issuer("authproxy")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        //토큰값 업데이트
        Member member = memberRepository.findFirstByLoginId(userName);
        member.setToken(refreshToken);

        return refreshToken;
    }

    public boolean validateToken(String token) {
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            var key = Keys.hmacShaKeyFor(keyBytes);
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date now = new Date();

            // 만료 검증 (exp)
            if (claims.getExpiration() == null || claims.getExpiration().before(now)) {
                return false;
            }
            // 발급시각(iat) 검증
            if (claims.getIssuedAt() != null && claims.getIssuedAt().after(now)) {
                return false;
            }
            // notBefore(nbf) 검증
            if (claims.getNotBefore() != null && claims.getNotBefore().after(now)) {
                return false;
            }
            // issuer(iss) 검증
            String expectedIssuer = "authproxy";
            if (claims.getIssuer() == null || !claims.getIssuer().equals(expectedIssuer)) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 실패·토큰 위조 등
            return false;
        }
    }

    public String getUserNameFromToken(String token) {
        try {
            return getClaimsFromToken(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Claims getClaimsFromToken(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        var key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    @Transactional
    public void deleteRefreshToken(String userName) {
        Member member = memberRepository.findFirstByLoginId(userName);
        if (member != null) {
            member.setToken(null); // 토큰을 null로 설정하여 삭제
        }
    }
}
