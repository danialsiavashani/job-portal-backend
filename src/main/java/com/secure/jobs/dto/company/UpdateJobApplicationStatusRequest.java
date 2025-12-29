package com.secure.jobs.dto.company;

import com.secure.jobs.models.job.JobApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateJobApplicationStatusRequest(
        @NotNull JobApplicationStatus status
) {}
