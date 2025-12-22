package com.secure.jobs.dto.company;

import java.time.LocalDateTime;

public record CompanyApplicationResponse(
        Long id,
        String companyName,
        String status,
        String documentUrl,
        LocalDateTime createdDate
) {
}
