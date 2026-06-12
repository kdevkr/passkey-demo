package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityJpaRepository extends JpaRepository<UserEntity, byte[]> {
    Optional<UserEntity> findByUsername(String username);
}
