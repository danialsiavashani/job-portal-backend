package com.secure.jobs;

import com.secure.jobs.models.user.auth.AppRole;
import com.secure.jobs.models.user.auth.PasswordResetToken;
import com.secure.jobs.models.user.auth.Role;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.PasswordResetTokenRepository;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.util.ResetTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for POST /api/auth/reset-password.
 *
 * Verifies that the three error paths (unknown token, expired token, already-used
 * token) all return 400 Bad Request instead of 500.  Also verifies the happy path
 * still returns 204 No Content.
 *
 * The endpoint is public (/api/auth/**), so no authentication header is needed.
 * @Transactional rolls back all DB changes after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordResetIT {

    private static final String RESET_URL = "/api/auth/reset-password";

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository                userRepository;
    @Autowired RoleRepository                roleRepository;
    @Autowired PasswordResetTokenRepository  tokenRepository;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User createUser() {
        Role role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName(AppRole.ROLE_USER).build()));

        return userRepository.save(User.builder()
                .username("reset_test_" + System.nanoTime())
                .email("reset_" + System.nanoTime() + "@test.com")
                .passwordHash("$2a$10$dummyHashForTestingOnly000000000000000000000000000000")
                .role(role)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .twoFactorEnabled(false)
                .build());
    }

    /** Creates a valid (unused, unexpired) token.  Returns the raw token string. */
    private String createValidToken(User user) {
        String raw  = ResetTokenUtil.generateRawToken();
        String hash = ResetTokenUtil.sha256Hex(raw);
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .usedAt(null)
                .build());
        return raw;
    }

    /** Creates an expired token (expiresAt in the past).  Returns the raw token. */
    private String createExpiredToken(User user) {
        String raw  = ResetTokenUtil.generateRawToken();
        String hash = ResetTokenUtil.sha256Hex(raw);
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))   // expired 1 h ago
                .usedAt(null)
                .build());
        return raw;
    }

    /** Creates an already-used token (usedAt set).  Returns the raw token. */
    private String createUsedToken(User user) {
        String raw  = ResetTokenUtil.generateRawToken();
        String hash = ResetTokenUtil.sha256Hex(raw);
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .expiresAt(Instant.now().plus(23, ChronoUnit.HOURS))
                .usedAt(Instant.now().minus(30, ChronoUnit.MINUTES))   // consumed 30 min ago
                .build());
        return raw;
    }

    private String resetBody(String token, String password) {
        return "{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\"}";
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void resetPassword_withBogusToken_returns400() throws Exception {
        // Token that was never stored — should NOT become 500
        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody("totally-bogus-token-that-does-not-exist", "NewPassword1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_withExpiredToken_returns400() throws Exception {
        String raw = createExpiredToken(createUser());

        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(raw, "NewPassword1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_withAlreadyUsedToken_returns400() throws Exception {
        String raw = createUsedToken(createUser());

        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(raw, "NewPassword1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_withValidToken_returns204() throws Exception {
        String raw = createValidToken(createUser());

        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(raw, "NewPassword1")))
                .andExpect(status().isNoContent());
    }
}
