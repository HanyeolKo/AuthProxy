package com.khy.authproxy.domain.manager.repository;

import com.khy.authproxy.domain.manager.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager,Integer> {
    Optional<Manager> findByLoginId(String loginId);
    Manager findFirstByEmail(String email);
    Manager findFirstByLoginId(String loginId);
}
