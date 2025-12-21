package com.secure.jobs.config;

import com.secure.jobs.models.AppRole;
import com.secure.jobs.models.Role;
import com.secure.jobs.models.User;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Bean
    CommandLineRunner seedData() {
        return args -> {

            // 1️⃣ Seed roles
            for (AppRole role : AppRole.values()) {
                roleRepository.findByRoleName(role)
                        .orElseGet(() -> roleRepository.save(new Role(role)));
            }

            // 2️⃣ Seed SUPER_ADMIN user (only once)
            if (!userRepository.existsByUsername("superAdmin")) {

                Role superAdminRole = roleRepository
                        .findByRoleName(AppRole.ROLE_SUPER_ADMIN)
                        .orElseThrow(); // safe, role just seeded

                User superAdmin = User.builder()
                        .username("superAdmin")
                        .email("superadmin@system.local")
                        .passwordHash(passwordEncoder.encode("superPass"))
                        .role(superAdminRole)
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();

                userRepository.save(superAdmin);

                System.out.println("✅ SUPER_ADMIN bootstrap user created");
            }
        };
    }
}
