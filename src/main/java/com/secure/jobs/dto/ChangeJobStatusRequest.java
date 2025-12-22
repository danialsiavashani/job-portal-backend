package com.secure.jobs.dto;

import com.secure.jobs.models.JobStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeJobStatusRequest(
        @NotNull JobStatus status
) {}
