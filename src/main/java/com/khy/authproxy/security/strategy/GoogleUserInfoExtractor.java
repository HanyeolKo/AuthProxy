package com.khy.authproxy.security.strategy;

import com.khy.authproxy.exception.NotFoundUserInfoException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component("google")
public class GoogleUserInfoExtractor implements  OAuth2UserInfoExtractor {
    @Override
    public String getName(OAuth2User user){
        if (user.getAttribute("name") != null) {
            return user.getAttribute("email");
        }
        throw new NotFoundUserInfoException("Google user name not found");
    }

    @Override
    public String getEmail(OAuth2User user) {
        if (user.getAttribute("email") != null) {
            return user.getAttribute("email");
        }
        throw new NotFoundUserInfoException("Google user email not found");
    }
}
