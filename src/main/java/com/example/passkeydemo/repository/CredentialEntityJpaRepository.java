package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialEntityJpaRepository extends JpaRepository<CredentialEntity, byte[]> {
    List<CredentialEntity> findByUserId(byte[] userId);
}
