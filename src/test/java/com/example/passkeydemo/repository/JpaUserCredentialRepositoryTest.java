package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.CredentialEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JpaUserCredentialRepository}의 CredentialRecord ↔ CredentialEntity
 * 매핑 라운드트립을 검증합니다. (Spring Security 7.1의 ImmutableCredentialRecord는
 * Serializable이 아니므로 Java 직렬화 기반 저장은 동작하지 않습니다.)
 */
class JpaUserCredentialRepositoryTest {

    private final CredentialEntityJpaRepository jpaRepo = mock(CredentialEntityJpaRepository.class);
    private final JpaUserCredentialRepository repository = new JpaUserCredentialRepository(jpaRepo);

    @Test
    void saveAndFindByCredentialId_roundTripsAllFields() {
        Bytes credentialId = new Bytes(new byte[] {1, 2, 3, 4});
        ImmutableCredentialRecord record = ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(credentialId)
                .userEntityUserId(new Bytes(new byte[] {5, 6, 7, 8}))
                .publicKey(new ImmutablePublicKeyCose(new byte[] {10, 20, 30}))
                .signatureCount(42L)
                .uvInitialized(true)
                .transports(Set.of(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.HYBRID))
                .backupEligible(true)
                .backupState(false)
                .attestationObject(new Bytes(new byte[] {90, 91}))
                .attestationClientDataJSON(new Bytes(new byte[] {70}))
                .created(Instant.parse("2026-06-13T00:00:00Z"))
                .lastUsed(Instant.parse("2026-06-13T01:00:00Z"))
                .label("test-passkey")
                .build();

        when(jpaRepo.findById(any())).thenReturn(Optional.empty());
        repository.save(record);

        ArgumentCaptor<CredentialEntity> captor = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(jpaRepo).save(captor.capture());
        when(jpaRepo.findById(any())).thenReturn(Optional.of(captor.getValue()));

        CredentialRecord loaded = repository.findByCredentialId(credentialId);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getCredentialType()).isEqualTo(PublicKeyCredentialType.PUBLIC_KEY);
        assertThat(loaded.getCredentialId().getBytes()).isEqualTo(record.getCredentialId().getBytes());
        assertThat(loaded.getUserEntityUserId().getBytes()).isEqualTo(record.getUserEntityUserId().getBytes());
        assertThat(loaded.getPublicKey().getBytes()).isEqualTo(record.getPublicKey().getBytes());
        assertThat(loaded.getSignatureCount()).isEqualTo(42L);
        assertThat(loaded.isUvInitialized()).isTrue();
        assertThat(loaded.getTransports())
                .containsExactlyInAnyOrder(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.HYBRID);
        assertThat(loaded.isBackupEligible()).isTrue();
        assertThat(loaded.isBackupState()).isFalse();
        assertThat(loaded.getAttestationObject().getBytes()).isEqualTo(record.getAttestationObject().getBytes());
        assertThat(loaded.getAttestationClientDataJSON().getBytes())
                .isEqualTo(record.getAttestationClientDataJSON().getBytes());
        assertThat(loaded.getCreated()).isEqualTo(record.getCreated());
        assertThat(loaded.getLastUsed()).isEqualTo(record.getLastUsed());
        assertThat(loaded.getLabel()).isEqualTo("test-passkey");
    }

    @Test
    void saveAndFindByCredentialId_handlesNullOptionalFields() {
        Bytes credentialId = new Bytes(new byte[] {9});
        ImmutableCredentialRecord record = ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(credentialId)
                .userEntityUserId(new Bytes(new byte[] {8}))
                .publicKey(new ImmutablePublicKeyCose(new byte[] {1}))
                .signatureCount(0L)
                .label("minimal")
                .build();

        when(jpaRepo.findById(any())).thenReturn(Optional.empty());
        repository.save(record);

        ArgumentCaptor<CredentialEntity> captor = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(jpaRepo).save(captor.capture());
        when(jpaRepo.findById(any())).thenReturn(Optional.of(captor.getValue()));

        CredentialRecord loaded = repository.findByCredentialId(credentialId);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getAttestationObject()).isNull();
        assertThat(loaded.getAttestationClientDataJSON()).isNull();
        assertThat(loaded.getTransports()).isNullOrEmpty();
    }
}
