package com.khy.authproxy.config.security.filter;

import com.khy.authproxy.config.security.jwt.JwtService;
import com.khy.authproxy.config.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // accessToken 쿠키에서 토큰을 찾음
        if(request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        } else {
            log.info("[JwtAuthFilter] 쿠키 없음");
        }

        //토큰 검증
        if(token != null && jwtService.validateToken(token)){
            String userName = jwtService.getUserNameFromToken(token);
            log.info("[JwtAuthFilter] 토큰 유효, userName: {}", userName);

            UserDetails userDetails = null;
            try{
            userDetails = userDetailsService.loadUserByUsername(userName);
            } catch (UsernameNotFoundException unfe) {          //토큰은 있지만 사용자가 없는경우
                Cookie accessCookie = new Cookie("accessToken", null);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(0); // 즉시 만료 처리
                accessCookie.setHttpOnly(true);

                Cookie refreshCookie = new Cookie("accessToken", null);
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(0); // 즉시 만료 처리
                refreshCookie.setHttpOnly(true);

                response.addCookie(accessCookie);
                response.addCookie(refreshCookie);

                response.sendRedirect("/");

                return;
            }

            //인증 객체 생성 및 SecurityContext에 저장
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.setAttribute("user", userDetails.getUsername());
        } else {
            log.info("[JwtAuthFilter] 토큰이 없거나 유효하지 않음");
        }

        filterChain.doFilter(request, response);
    }
}
