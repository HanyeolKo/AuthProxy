package com.khy.authproxy.config.security.handler;

import com.khy.authproxy.domain.member.entity.Member;
import com.khy.authproxy.domain.member.repository.MemberRepository;
import com.khy.authproxy.config.security.jwt.JwtService;
import com.khy.authproxy.config.security.strategy.OAuth2UserInfoExtractorFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final MemberRepository memberRepository;
    private final OAuth2UserInfoExtractorFactory extractorFactory;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Value("${jwt.ACCESS_TOKEN_VALID_TIME}")
    private long accessTokenExpireTime;
    @Value(("${jwt.REFRESH_TOKEN_VALID_TIME}"))
    private long refreshTokenExpireTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        //공급자 식별 및 사용자 정보 추출
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 전략 팩토리로 공급자별 email 추출
        String email = extractorFactory.get(registrationId).getEmail(oAuth2User);

        Member userMember = memberRepository.findFirstByEmail(email);

        // 사용자 정보가 존재하지 않는 경우, 새로운 사용자 생성
        if (userMember == null) {
            String name = extractorFactory.get(registrationId).getName(oAuth2User);
            String loginId = email.split("@")[0]; // 이메일의 '@' 앞부분을 로그인 ID로 사용
            String password = passwordEncoder.encode(UUID.randomUUID().toString().substring(0,10)); // 임시 비밀번호 생성 (UUID의 일부를 사용)
            userMember = Member.builder()
                    .loginId(loginId)
                    .password(password) // 임시 비밀번호 생성
                    .email(email)
                    .name(name)
                    .build();
            memberRepository.save(userMember);
        }

        String refreshToken = jwtService.createRefreshToken(userMember.getLoginId());
        String accessToken = jwtService.createAccessToken(refreshToken);

        //로그인 처리
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setMaxAge((int) accessTokenExpireTime / 1000); // 쿠키의 유효 기간을 초 단위로 설정
        accessCookie.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
        accessCookie.setSecure(true); // HTTPS에서만 전송되도록 설정
        accessCookie.setPath("/");

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setMaxAge((int) refreshTokenExpireTime / 1000); // 쿠키의 유효 기간을 초 단위로 설정
        refreshCookie.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
        refreshCookie.setSecure(true); // HTTPS에서만 전송되도록 설정
        refreshCookie.setPath("/");

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.sendRedirect("/");
    }
}
