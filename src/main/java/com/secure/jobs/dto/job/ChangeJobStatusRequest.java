package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.JobStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeJobStatusRequest(
        @NotNull JobStatus status
) {}
