package com.khy.authproxy.domain.member.repository;

import com.khy.authproxy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Integer> {
    Optional<Member> findByLoginId(String loginId);
    Member findFirstByEmail(String email);
    Member findFirstByLoginId(String loginId);
}
