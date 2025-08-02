package com.khy.authproxy.security.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2UserInfoExtractorFactory {
    private final Map<String, OAuth2UserInfoExtractor> strategyMap;

    public OAuth2UserInfoExtractorFactory(Map<String, OAuth2UserInfoExtractor> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public OAuth2UserInfoExtractor get(String registrationId) {
        OAuth2UserInfoExtractor extractor = strategyMap.get(registrationId);
        if (extractor == null) {
            throw new IllegalArgumentException("Unsupported OAuth2 Provider: " + registrationId);
        }
        return extractor;
    }
}
