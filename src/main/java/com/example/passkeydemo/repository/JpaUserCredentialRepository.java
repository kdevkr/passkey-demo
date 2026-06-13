package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.CredentialEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA-backed implementation of {@link UserCredentialRepository}.
 *
 * <p>{@link CredentialRecord}는 필드 단위로 컬럼에 매핑하여 저장하고,
 * 조회 시 {@link ImmutableCredentialRecord#builder()}로 복원합니다.
 * (Spring Security 7의 {@code ImmutableCredentialRecord}는 {@code Serializable}을
 * 구현하지 않으므로 Java 직렬화는 사용할 수 없습니다.)
 */
@Repository
public class JpaUserCredentialRepository implements UserCredentialRepository {

    private static final String TRANSPORT_DELIMITER = ",";

    private final CredentialEntityJpaRepository jpaRepo;

    public JpaUserCredentialRepository(@NonNull CredentialEntityJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(@NonNull CredentialRecord record) {
        byte[] credId = record.getCredentialId().getBytes();

        CredentialEntity entity = jpaRepo.findById(credId)
                .orElse(new CredentialEntity());
        entity.setCredentialId(credId);
        entity.setUserId(record.getUserEntityUserId().getBytes());
        entity.setLabel(record.getLabel());
        entity.setCredentialType(record.getCredentialType().getValue());
        entity.setPublicKeyCose(record.getPublicKey().getBytes());
        entity.setSignatureCount(record.getSignatureCount());
        entity.setUvInitialized(record.isUvInitialized());
        entity.setTransports(joinTransports(record.getTransports()));
        entity.setBackupEligible(record.isBackupEligible());
        entity.setBackupState(record.isBackupState());
        entity.setAttestationObject(toByteArray(record.getAttestationObject()));
        entity.setAttestationClientDataJson(toByteArray(record.getAttestationClientDataJSON()));
        entity.setCreated(record.getCreated());
        entity.setLastUsed(record.getLastUsed());
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

    // --- Mapping helpers ---

    private @NonNull CredentialRecord toCredentialRecord(@NonNull CredentialEntity entity) {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.valueOf(entity.getCredentialType()))
                .credentialId(new Bytes(entity.getCredentialId()))
                .userEntityUserId(new Bytes(entity.getUserId()))
                .publicKey(new ImmutablePublicKeyCose(entity.getPublicKeyCose()))
                .signatureCount(entity.getSignatureCount())
                .uvInitialized(entity.isUvInitialized())
                .transports(splitTransports(entity.getTransports()))
                .backupEligible(entity.isBackupEligible())
                .backupState(entity.isBackupState())
                .attestationObject(toBytes(entity.getAttestationObject()))
                .attestationClientDataJSON(toBytes(entity.getAttestationClientDataJson()))
                .created(entity.getCreated())
                .lastUsed(entity.getLastUsed())
                .label(entity.getLabel())
                .build();
    }

    private @Nullable String joinTransports(@Nullable Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return null;
        }
        return transports.stream()
                .map(AuthenticatorTransport::getValue)
                .collect(Collectors.joining(TRANSPORT_DELIMITER));
    }

    private @NonNull Set<AuthenticatorTransport> splitTransports(@Nullable String transports) {
        if (transports == null || transports.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(transports.split(TRANSPORT_DELIMITER))
                .map(AuthenticatorTransport::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private byte @Nullable [] toByteArray(@Nullable Bytes bytes) {
        return bytes != null ? bytes.getBytes() : null;
    }

    private @Nullable Bytes toBytes(byte @Nullable [] bytes) {
        return bytes != null ? new Bytes(bytes) : null;
    }
}
