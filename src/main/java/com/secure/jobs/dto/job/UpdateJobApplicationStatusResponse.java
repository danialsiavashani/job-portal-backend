package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.JobApplicationStatus;

import java.time.LocalDateTime;

public record UpdateJobApplicationStatusResponse(
        Long applicationId,
        JobApplicationStatus status,
        LocalDateTime updatedAt // optional but useful
) {}
