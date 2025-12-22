package com.secure.jobs.services;

import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.models.job.JobApplication;

public interface JobApplicationService {
    JobApplication apply(Long userId, Long jobId, JobApplicationRequest request);
}
