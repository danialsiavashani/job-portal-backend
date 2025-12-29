package com.secure.jobs.dto.company;

public record CompanyProfileResponse(
        Long id,
        Long userId,
        String userName,
        String name,
        boolean enabled
) {
}
