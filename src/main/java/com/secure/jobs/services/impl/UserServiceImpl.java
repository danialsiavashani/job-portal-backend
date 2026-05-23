package com.secure.jobs.services.impl;

import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
import com.secure.jobs.dto.admin.UpdateUserModerationResponse;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.AdminModerationMapper;
import com.secure.jobs.models.user.auth.PasswordResetToken;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.PasswordResetTokenRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.util.ResetTokenUtil;
import com.secure.jobs.services.EmailService;
import com.secure.jobs.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    @Override
    public User getMe(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UpdateUserModerationResponse patchModeration(Long userId, UpdateUserModerationRequest request) {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));
            boolean changed = false;

            if(request.getEnabled() != null){
                user.setEnabled(request.getEnabled());
                changed = true;
            }
        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
            changed = true;
        }
        if (!changed) {
            throw new ApiException("No moderation fields provided", HttpStatus.BAD_REQUEST);
        }
        return AdminModerationMapper.toUserModerationResponse(user);
    }



    @Override
    @Transactional
    public void generatePasswordResetToken(String email) {

        // ✅ do NOT reveal whether email exists
        userRepository.findByEmail(email).ifPresent(user -> {

            // optional cleanup: invalidate previous unused tokens for this user
            passwordResetTokenRepository.deleteByUser_UserIdAndUsedAtIsNull(user.getUserId());

            String rawToken = ResetTokenUtil.generateRawToken();
            String tokenHash = ResetTokenUtil.sha256Hex(rawToken);

            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .usedAt(null)
                    .build();

            passwordResetTokenRepository.save(token);

            String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        String tokenHash = ResetTokenUtil.sha256Hex(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException("Password reset token has expired");
        }

        User user = resetToken.getUser();

        // optional: block disabled users from resetting password
        // if (!user.isEnabled()) throw new RuntimeException("Account disabled");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        // optional cleanup: delete expired tokens
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
