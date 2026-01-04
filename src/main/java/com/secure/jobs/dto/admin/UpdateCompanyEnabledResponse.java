package com.secure.jobs.dto.admin;

public record UpdateCompanyEnabledResponse(
        Long companyId,
        boolean enabled
){}
