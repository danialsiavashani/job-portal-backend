package com.secure.jobs.dto.admin;

public record UpdateUserModerationResponse(
        Long userId,
        boolean enabled,
        boolean accountNonLocked
) {}
