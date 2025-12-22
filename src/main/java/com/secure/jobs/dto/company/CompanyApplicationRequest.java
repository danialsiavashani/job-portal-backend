package com.secure.jobs.dto.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyApplicationRequest(
        @NotBlank String companyName,
        @NotBlank String documentPublicId,
        @NotBlank String documentUrl
) {}