package com.secure.jobs.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyApplicationRequest(
        @NotBlank String companyName,
        @NotBlank String documentPublicId,
        @NotBlank String documentUrl
) {}