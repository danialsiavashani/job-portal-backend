package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.JobStatus;

public record ChangeJobStatusResponse(
        Long jobId,
        JobStatus status
) {}
