package com.secure.jobs.config;

import com.secure.jobs.models.AppRole;
import com.secure.jobs.models.Role;
import com.secure.jobs.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Autowired
    RoleRepository roleRepository;

    @Bean
    CommandLineRunner seedRoles() {
        return args -> {
            for (AppRole role : AppRole.values()) {
                roleRepository.findByRoleName(role)
                        .orElseGet(() -> roleRepository.save(new Role(role)));
            }
        };
    }
}
