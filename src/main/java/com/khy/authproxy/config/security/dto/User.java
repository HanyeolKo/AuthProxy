package com.khy.authproxy.config.security.dto;

import lombok.Builder;

@Builder
public record User(
        String username,
        String userId,
        String role,
        boolean access
) {
}
