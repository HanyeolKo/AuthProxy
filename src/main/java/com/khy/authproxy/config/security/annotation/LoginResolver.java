package com.khy.authproxy.config.security.annotation;

import com.khy.authproxy.config.security.dto.User;
import com.khy.authproxy.config.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginResolver implements HandlerMethodArgumentResolver {

    private final JwtService jwtService;

    @Override
    public boolean supportsParameter(MethodParameter param) {
        boolean hasAnnotation = param.hasParameterAnnotation(LoginUser.class);
        Class<?> type = param.getParameterType();
        boolean supportedType = type.isAssignableFrom(String.class) || type.isAssignableFrom(User.class);
        return hasAnnotation && supportedType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final Map<String, Object> resolved = new HashMap<>();

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .map(Cookie::getValue).findFirst().ifPresent(accessToken -> {
                    Claims claims = jwtService.getClaimsFromToken(accessToken);

                    if(parameter.getParameterType().isAssignableFrom(String.class)) {
                        resolved.put("resolved", claims.getSubject());
                    }
                    else if(parameter.getParameterType().isAssignableFrom(User.class)) {
                        User user = User.builder()
                                .userId(claims.getSubject())
                                .username(claims.getSubject())
                                .role("USER")
                                .access(true)
                                .build();
                        resolved.put("resolved", user);
                    } else {
                        throw new IllegalArgumentException("Unsupported parameter type: " + parameter.getParameterType());
                    }
                });

        return resolved.get("resolved");
    }
}
