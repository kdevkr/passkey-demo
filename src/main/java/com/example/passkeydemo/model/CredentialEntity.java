package com.example.passkeydemo.model;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;

/**
 * JPA entity for persisting WebAuthn credential records.
 *
 * <p>{@code CredentialRecord}의 모든 필드를 개별 컬럼으로 저장합니다.
 * Spring Security 7의 {@code ImmutableCredentialRecord}는 {@code Serializable}을
 * 구현하지 않으므로 Java 직렬화 대신 필드 단위 매핑을 사용하며,
 * 조회 시 {@code ImmutableCredentialRecord.builder()}로 복원합니다.
 */
@Entity
@Table(name = "passkey_credentials")
public class CredentialEntity {

    @Id
    @Column(name = "credential_id", nullable = false, columnDefinition = "BLOB")
    private byte[] credentialId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BLOB")
    private byte[] userId;

    @Column(name = "label")
    private @Nullable String label;

    /** {@code PublicKeyCredentialType.getValue()} — 현재 스펙상 "public-key" 고정. */
    @Column(name = "credential_type", nullable = false)
    private String credentialType;

    /** COSE-encoded public key bytes. */
    @Column(name = "public_key_cose", nullable = false, columnDefinition = "BLOB")
    private byte[] publicKeyCose;

    @Column(name = "signature_count", nullable = false)
    private long signatureCount;

    @Column(name = "uv_initialized", nullable = false)
    private boolean uvInitialized;

    /** {@code AuthenticatorTransport.getValue()} 값들의 콤마 구분 문자열 (예: "internal,hybrid"). */
    @Column(name = "transports")
    private @Nullable String transports;

    @Column(name = "backup_eligible", nullable = false)
    private boolean backupEligible;

    @Column(name = "backup_state", nullable = false)
    private boolean backupState;

    @Column(name = "attestation_object", columnDefinition = "BLOB")
    private byte @Nullable [] attestationObject;

    @Column(name = "attestation_client_data_json", columnDefinition = "BLOB")
    private byte @Nullable [] attestationClientDataJson;

    @Column(name = "created")
    private @Nullable Instant created;

    @Column(name = "last_used")
    private @Nullable Instant lastUsed;

    public CredentialEntity() {}

    public byte @NonNull [] getCredentialId() { return credentialId; }
    public void setCredentialId(byte @NonNull [] credentialId) { this.credentialId = credentialId; }

    public byte @NonNull [] getUserId() { return userId; }
    public void setUserId(byte @NonNull [] userId) { this.userId = userId; }

    public @Nullable String getLabel() { return label; }
    public void setLabel(@Nullable String label) { this.label = label; }

    public @NonNull String getCredentialType() { return credentialType; }
    public void setCredentialType(@NonNull String credentialType) { this.credentialType = credentialType; }

    public byte @NonNull [] getPublicKeyCose() { return publicKeyCose; }
    public void setPublicKeyCose(byte @NonNull [] publicKeyCose) { this.publicKeyCose = publicKeyCose; }

    public long getSignatureCount() { return signatureCount; }
    public void setSignatureCount(long signatureCount) { this.signatureCount = signatureCount; }

    public boolean isUvInitialized() { return uvInitialized; }
    public void setUvInitialized(boolean uvInitialized) { this.uvInitialized = uvInitialized; }

    public @Nullable String getTransports() { return transports; }
    public void setTransports(@Nullable String transports) { this.transports = transports; }

    public boolean isBackupEligible() { return backupEligible; }
    public void setBackupEligible(boolean backupEligible) { this.backupEligible = backupEligible; }

    public boolean isBackupState() { return backupState; }
    public void setBackupState(boolean backupState) { this.backupState = backupState; }

    public byte @Nullable [] getAttestationObject() { return attestationObject; }
    public void setAttestationObject(byte @Nullable [] attestationObject) { this.attestationObject = attestationObject; }

    public byte @Nullable [] getAttestationClientDataJson() { return attestationClientDataJson; }
    public void setAttestationClientDataJson(byte @Nullable [] attestationClientDataJson) { this.attestationClientDataJson = attestationClientDataJson; }

    public @Nullable Instant getCreated() { return created; }
    public void setCreated(@Nullable Instant created) { this.created = created; }

    public @Nullable Instant getLastUsed() { return lastUsed; }
    public void setLastUsed(@Nullable Instant lastUsed) { this.lastUsed = lastUsed; }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof CredentialEntity that)) return false;
        return Arrays.equals(this.credentialId, that.credentialId);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.credentialId);
    }
}
