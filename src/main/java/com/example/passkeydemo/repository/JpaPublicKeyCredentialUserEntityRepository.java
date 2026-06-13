package com.example.passkeydemo.repository;

import com.example.passkeydemo.model.UserEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPublicKeyCredentialUserEntityRepository
        implements PublicKeyCredentialUserEntityRepository {

    private final UserEntityJpaRepository jpaRepo;

    public JpaPublicKeyCredentialUserEntityRepository(@NonNull UserEntityJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public @Nullable PublicKeyCredentialUserEntity findByUsername(@NonNull String username) {
        return jpaRepo.findByUsername(username).orElse(null);
    }

    @Override
    public @Nullable PublicKeyCredentialUserEntity findById(@NonNull Bytes id) {
        return jpaRepo.findById(id.getBytes()).orElse(null);
    }

    @Override
    public void save(@NonNull PublicKeyCredentialUserEntity userEntity) {
        byte[] id = userEntity.getId().getBytes();
        UserEntity entity = jpaRepo.findById(id).orElse(new UserEntity());
        entity.setId(id);
        entity.setUsername(userEntity.getName());
        // getDisplayName()은 @Nullable이므로 null이면 username으로 폴백한다.
        String displayName = userEntity.getDisplayName();
        entity.setDisplayName(displayName != null ? displayName : userEntity.getName());
        jpaRepo.save(entity);
    }

    @Override
    public void delete(@NonNull Bytes id) {
        jpaRepo.deleteById(id.getBytes());
    }
}
