package com.secure.jobs.config;

import com.secure.jobs.models.user.auth.AppRole;
import com.secure.jobs.models.user.auth.Role;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.admin.username:}")
    private String bootstrapUsername;

    @Value("${app.bootstrap.admin.email:}")
    private String bootstrapEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String bootstrapPassword;

    @Bean
    CommandLineRunner seedData() {
        return args -> {

            // 1️⃣ Seed roles (always runs)
            for (AppRole role : AppRole.values()) {
                roleRepository.findByRoleName(role)
                        .orElseGet(() -> roleRepository.save(new Role(role)));
            }

            // 2️⃣ Seed SUPER_ADMIN user (only if bootstrap is enabled)
            if (!bootstrapEnabled) {
                return;
            }

            if (bootstrapUsername.isBlank() || bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
                System.out.println("⚠️  SUPER_ADMIN bootstrap skipped: username, email, or password property is blank.");
                return;
            }

            if (!userRepository.existsByUsername(bootstrapUsername)) {

                Role superAdminRole = roleRepository
                        .findByRoleName(AppRole.ROLE_SUPER_ADMIN)
                        .orElseThrow(); // safe, role just seeded

                User superAdmin = User.builder()
                        .username(bootstrapUsername)
                        .email(bootstrapEmail)
                        .passwordHash(passwordEncoder.encode(bootstrapPassword))
                        .role(superAdminRole)
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();

                userRepository.save(superAdmin);

                System.out.println("✅ SUPER_ADMIN bootstrap user created: " + bootstrapUsername);
            }
        };
    }
}
