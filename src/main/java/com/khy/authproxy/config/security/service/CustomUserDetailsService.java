package com.khy.authproxy.config.security.service;

import com.khy.authproxy.domain.member.entity.Member;
import com.khy.authproxy.domain.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findFirstByLoginId(username);
        if (member == null) {
            throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + username);
        }
        return new CustomUserDetails(member);
    }

    @RequiredArgsConstructor
    public static class CustomUserDetails implements UserDetails {

        private final Member member;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // 권한이 필요하다면 여기서 추가
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getPassword() {
            return member.getPassword();
        }

        @Override
        public String getUsername() {
            return member.getLoginId();
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
            return member.isActive();
        }

        public Member getMember() {
            return member;
        }
    }
}

