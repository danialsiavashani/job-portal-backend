package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.JobApplicationStatus;

import java.time.LocalDateTime;

public record JobApplicationResponse (
    Long applicationId,
    Long jobId,
    String jobTitle,
    String companyName,
    JobApplicationStatus status,
    LocalDateTime appliedAt
){}
