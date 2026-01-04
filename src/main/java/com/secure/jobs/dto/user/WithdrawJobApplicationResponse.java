package com.secure.jobs.dto.user;

import com.secure.jobs.models.job.JobApplicationStatus;

public record WithdrawJobApplicationResponse(
        Long userId,
        JobApplicationStatus status
) {}
