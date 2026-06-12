package com.example.passkeydemo.model;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;

import java.util.Arrays;

@Entity
@Table(name = "passkey_users")
public class UserEntity implements PublicKeyCredentialUserEntity {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "BLOB")
    private byte[] id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    public UserEntity() {}

    public UserEntity(byte @NonNull [] id, @NonNull String username, @NonNull String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    // --- PublicKeyCredentialUserEntity ---

    @Override
    public @NonNull Bytes getId() {
        return new Bytes(this.id);
    }

    @Override
    public @NonNull String getName() {
        return this.username;
    }

    @Override
    public @NonNull String getDisplayName() {
        return this.displayName;
    }

    // --- JPA accessors ---

    public byte @NonNull [] getRawId() {
        return id;
    }

    public void setId(byte @NonNull [] id) {
        this.id = id;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return Arrays.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.id);
    }
}
