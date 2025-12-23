package com.secure.jobs.dto.company;

import com.secure.jobs.models.job.JobApplicationStatus;

import java.time.LocalDateTime;

public record CompanyJobApplicationRowResponse(
        Long applicationId,
        Long jobId,
        String jobTitle,
        Long applicantUserId,
        String applicantUsername,
        String applicantEmail,
        JobApplicationStatus status,
        LocalDateTime appliedAt
) {
}
