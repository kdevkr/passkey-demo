package com.example.passkeydemo.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 SQLite DB를 통과하는 저장/조회 통합 테스트.
 * Hibernate 커뮤니티 방언의 Instant/BLOB 매핑이 실제로 동작하는지 검증합니다.
 */
@SpringBootTest
class JpaUserCredentialRepositoryIntegrationTest {

    private static final Bytes CREDENTIAL_ID = new Bytes(new byte[] {1, 2, 3, 4, 5});
    private static final Bytes USER_ID = new Bytes(new byte[] {9, 8, 7, 6});

    @Autowired
    private JpaUserCredentialRepository repository;

    @AfterEach
    void cleanUp() {
        repository.delete(CREDENTIAL_ID);
    }

    @Test
    void saveAndLoadThroughSqlite() {
        ImmutableCredentialRecord record = ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(CREDENTIAL_ID)
                .userEntityUserId(USER_ID)
                .publicKey(new ImmutablePublicKeyCose(new byte[] {10, 20, 30}))
                .signatureCount(7L)
                .uvInitialized(true)
                .transports(Set.of(AuthenticatorTransport.INTERNAL))
                .backupEligible(true)
                .backupState(true)
                .attestationObject(new Bytes(new byte[] {42}))
                .attestationClientDataJSON(new Bytes(new byte[] {24}))
                .created(Instant.parse("2026-06-13T00:00:00Z"))
                .lastUsed(Instant.parse("2026-06-13T01:00:00Z"))
                .label("integration-passkey")
                .build();

        repository.save(record);

        CredentialRecord byId = repository.findByCredentialId(CREDENTIAL_ID);
        assertThat(byId).isNotNull();
        assertThat(byId.getLabel()).isEqualTo("integration-passkey");
        assertThat(byId.getSignatureCount()).isEqualTo(7L);
        assertThat(byId.getPublicKey().getBytes()).isEqualTo(new byte[] {10, 20, 30});
        assertThat(byId.getTransports()).containsExactly(AuthenticatorTransport.INTERNAL);
        assertThat(byId.getCreated()).isEqualTo(Instant.parse("2026-06-13T00:00:00Z"));
        assertThat(byId.getLastUsed()).isEqualTo(Instant.parse("2026-06-13T01:00:00Z"));

        List<CredentialRecord> byUser = repository.findByUserId(USER_ID);
        assertThat(byUser).hasSize(1);
        assertThat(byUser.getFirst().getCredentialId().getBytes()).isEqualTo(CREDENTIAL_ID.getBytes());

        // signature_count 갱신(재인증 시나리오) — upsert 경로 검증
        repository.save(ImmutableCredentialRecord.fromCredentialRecord(record).signatureCount(8L).build());
        CredentialRecord updated = repository.findByCredentialId(CREDENTIAL_ID);
        assertThat(updated).isNotNull();
        assertThat(updated.getSignatureCount()).isEqualTo(8L);
    }
}
