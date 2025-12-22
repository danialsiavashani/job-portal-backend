package com.secure.jobs.auth.dto;

import java.time.LocalDateTime;

public record CompanyApplicationResponse(
        Long id,
        String companyName,
        String status,
        String documentUrl,
        LocalDateTime createdDate
) {
}
