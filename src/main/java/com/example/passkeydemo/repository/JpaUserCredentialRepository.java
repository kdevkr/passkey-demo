package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.CredentialEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.Base64;
import java.util.List;

/**
 * JPA-backed implementation of {@link UserCredentialRepository}.
 *
 * <p>{@link CredentialRecord} 객체는 Java 직렬화(ObjectOutputStream)로 Base64 인코딩하여
 * SQLite TEXT 컬럼에 저장합니다.
 * Spring Security의 {@code ImmutableCredentialRecord}는 {@link Serializable}을 구현하므로
 * 내부 COSEKey, AttestationObject 등 모든 타입이 정상적으로 직렬화됩니다.
 */
@Repository
public class JpaUserCredentialRepository implements UserCredentialRepository {

    private final CredentialEntityJpaRepository jpaRepo;

    public JpaUserCredentialRepository(@NonNull CredentialEntityJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(@NonNull CredentialRecord record) {
        byte[] credId = record.getCredentialId().getBytes();
        byte[] userId = record.getUserEntityUserId().getBytes();

        String recordClass = record.getClass().getName();
        String recordData = serialize(record);

        CredentialEntity entity = jpaRepo.findById(credId)
                .orElse(new CredentialEntity());
        entity.setCredentialId(credId);
        entity.setUserId(userId);
        entity.setLabel(record.getLabel());
        entity.setRecordClass(recordClass);
        entity.setRecordData(recordData);
        jpaRepo.save(entity);
    }

    @Override
    public @NonNull List<CredentialRecord> findByUserId(@NonNull Bytes userId) {
        return jpaRepo.findByUserId(userId.getBytes()).stream()
                .map(this::toCredentialRecord)
                .toList();
    }

    @Override
    public @Nullable CredentialRecord findByCredentialId(@NonNull Bytes credentialId) {
        return jpaRepo.findById(credentialId.getBytes())
                .map(this::toCredentialRecord)
                .orElse(null);
    }

    @Override
    public void delete(@NonNull Bytes id) {
        jpaRepo.deleteById(id.getBytes());
    }

    // --- Serialization helpers ---

    private @NonNull String serialize(@NonNull CredentialRecord record) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(record);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("CredentialRecord 직렬화 실패", e);
        }
    }

    private @NonNull CredentialRecord toCredentialRecord(@NonNull CredentialEntity entity) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(
                Base64.getDecoder().decode(entity.getRecordData()));
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (CredentialRecord) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("CredentialRecord 역직렬화 실패 (id="
                    + Base64.getEncoder().encodeToString(entity.getCredentialId()) + ")", e);
        }
    }
}
