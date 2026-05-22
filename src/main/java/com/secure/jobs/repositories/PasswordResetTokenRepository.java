package com.secure.jobs.repositories;

import com.secure.jobs.models.user.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteByUser_UserIdAndUsedAtIsNull(Long userId);

    void deleteByExpiresAtBefore(Instant threshold);
}
