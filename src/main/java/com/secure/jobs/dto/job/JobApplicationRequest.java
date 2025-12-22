package com.secure.jobs.dto.job;

import jakarta.validation.constraints.NotBlank;

public record JobApplicationRequest(
        @NotBlank String documentPublicId,
        @NotBlank String documentUrl
) {}
