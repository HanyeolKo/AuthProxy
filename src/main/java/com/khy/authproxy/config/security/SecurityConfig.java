package com.khy.authproxy.config.security;

import com.khy.authproxy.config.security.filter.JwtAuthFilter;
import com.khy.authproxy.config.security.handler.OAuthFailedHandler;
import com.khy.authproxy.config.security.handler.OAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final OAuthFailedHandler oAuthFailedHandler;

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) ->
                auth
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/h2/**", "/login").permitAll()
                        .anyRequest().authenticated()
                );

        http.oauth2Login(oauth2 ->
                oauth2
                        .loginPage("/login")
                        .successHandler(oAuthSuccessHandler)
                        .failureHandler(oAuthFailedHandler)
                        .permitAll()
        );

        http.logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // JWT 필터 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)); // H2 콘솔 사용 시 프레임 옵션 설정
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2/**")); // H2 콘솔 사용 시 CSRF 비활성화
        return http.build();

    }

}
