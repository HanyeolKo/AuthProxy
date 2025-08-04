package com.khy.authproxy.config.security.service;

import com.khy.authproxy.domain.manager.entity.Manager;
import com.khy.authproxy.domain.manager.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Manager manager = managerRepository.findFirstByLoginId(username);
        if (manager == null) {
            throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + username);
        }
        return new CustomUserDetails(manager);
    }

    @RequiredArgsConstructor
    public static class CustomUserDetails implements UserDetails {

        private final Manager manager;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // 권한이 필요하다면 여기서 추가
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getPassword() {
            return manager.getPassword();
        }

        @Override
        public String getUsername() {
            return manager.getLoginId();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return manager.isActive();
        }

        public Manager getManager() {
            return manager;
        }
    }
}

