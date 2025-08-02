package com.khy.authproxy.security.handler;

import com.khy.authproxy.domain.manager.entity.Manager;
import com.khy.authproxy.domain.manager.repository.ManagerRepository;
import com.khy.authproxy.security.strategy.OAuth2UserInfoExtractorFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final ManagerRepository managerRepository;
    private final OAuth2UserInfoExtractorFactory extractorFactory;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        //공급자 식별 및 사용자 정보 추출
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 전략 팩토리로 공급자별 email 추출
        String email = extractorFactory.get(registrationId).getEmail(oAuth2User);

        Manager managerUser = managerRepository.findFirstByEmail(email);

        // 사용자 정보가 존재하지 않는 경우, 새로운 사용자 생성
        if (managerUser == null) {
            String name = extractorFactory.get(registrationId).getName(oAuth2User);
            String loginId = email.split("@")[0]; // 이메일의 '@' 앞부분을 로그인 ID로 사용
            String password = passwordEncoder.encode(UUID.randomUUID().toString().substring(0,10)); // 임시 비밀번호 생성 (UUID의 일부를 사용)
            managerUser = Manager.builder()
                    .loginId(loginId)
                    .password(password) // 임시 비밀번호 생성
                    .email(email)
                    .name(name)
                    .build();
            managerRepository.save(managerUser);
        }
        
        //로그인 처리(jwt, 세션 아직 미정 jwt 유력)

        response.sendRedirect("/");
    }
}
