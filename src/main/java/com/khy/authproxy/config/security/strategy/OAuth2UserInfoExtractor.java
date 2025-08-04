package com.khy.authproxy.config.security.strategy;

import com.khy.authproxy.exception.NotFoundUserInfoException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor{
    String getName(OAuth2User user) throws NotFoundUserInfoException;
    String getEmail(OAuth2User user) throws NotFoundUserInfoException;
}
