package com.secure.jobs.dto.user;

public record MeBasicResponse(
        Long id,
        String username,
        String email,
        String role,
        boolean enabled
) {}
