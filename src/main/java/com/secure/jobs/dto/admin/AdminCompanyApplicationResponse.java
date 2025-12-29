package com.secure.jobs.dto.admin;

import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.job.JobApplicationStatus;

import java.time.LocalDateTime;

public record AdminCompanyApplicationResponse(
        Long applicationId,
        Long applicantUserId,
        String applicantUsername,
        String applicantEmail,
        String documentUrl,
        String companyName,
        CompanyApplicationStatus status,
        LocalDateTime appliedAt
) {
}
