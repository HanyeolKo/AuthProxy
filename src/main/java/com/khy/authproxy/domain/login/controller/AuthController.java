package com.khy.authproxy.domain.login.controller;

import com.khy.authproxy.config.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @Value("${jwt.ACCESS_TOKEN_VALID_TIME}")
    private long accessTokenExpireTime;

    @GetMapping("/reissue")
    public ResponseEntity<?> reissueFromRefreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw new UsernameNotFoundException("Refresh token is required");
        }

        String accessToken = jwtService.createAccessToken(refreshToken);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) accessTokenExpireTime / 1000); // 초 단위로 설정
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정

        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok("Access token reissued successfully");

    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            Arrays.stream(request.getCookies()).forEach((cookie) -> {
                if (cookie.getName().equals("accessToken") || cookie.getName().equals("refreshToken")) {

                    jwtService.deleteRefreshToken(jwtService.getUserNameFromToken(cookie.getValue()));

                    Cookie logoutCookie = new Cookie(cookie.getName(), null);
                    logoutCookie.setPath("/");
                    logoutCookie.setMaxAge(0); // 즉시 만료 처리
                    logoutCookie.setHttpOnly(true);
                    logoutCookie.setSecure(true); // HTTPS에서만 전송되도록 설정
                    response.addCookie(logoutCookie);
                }
            });
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Logout failed: " + e.getMessage());
        }

        return ResponseEntity.ok("Logout successfully");

    }
}
