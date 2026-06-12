package com.example.passkeydemo.model;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * JPA entity for persisting WebAuthn credential records.
 *
 * <p>The full {@code CredentialRecord} is serialized via Java Serialization and stored
 * as a Base64-encoded string, alongside simple query fields (credentialId, userId).
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

    /** Fully-qualified class name of the {@code CredentialRecord} implementation. */
    @Column(name = "record_class", nullable = false)
    private String recordClass;

    /** Java-serialized {@code CredentialRecord} encoded as Base64 text. */
    @Column(name = "record_data", nullable = false, columnDefinition = "TEXT")
    private String recordData;

    public CredentialEntity() {}

    public CredentialEntity(
            byte @NonNull [] credentialId,
            byte @NonNull [] userId,
            @Nullable String label,
            @NonNull String recordClass,
            @NonNull String recordData) {
        this.credentialId = credentialId;
        this.userId = userId;
        this.label = label;
        this.recordClass = recordClass;
        this.recordData = recordData;
    }

    public byte @NonNull [] getCredentialId() { return credentialId; }
    public void setCredentialId(byte @NonNull [] credentialId) { this.credentialId = credentialId; }

    public byte @NonNull [] getUserId() { return userId; }
    public void setUserId(byte @NonNull [] userId) { this.userId = userId; }

    public @Nullable String getLabel() { return label; }
    public void setLabel(@Nullable String label) { this.label = label; }

    public @NonNull String getRecordClass() { return recordClass; }
    public void setRecordClass(@NonNull String recordClass) { this.recordClass = recordClass; }

    public @NonNull String getRecordData() { return recordData; }
    public void setRecordData(@NonNull String recordData) { this.recordData = recordData; }

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
