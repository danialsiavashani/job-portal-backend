package com.secure.jobs.services;

import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;

import java.util.List;

public interface JobApplicationService {
    JobApplication apply(Long userId, Long jobId, JobApplicationRequest request);

    void moveToInterview(Long applicationId, Long companyUserId);

    void reject(Long applicationId, Long companyUserId);

    List<JobApplicationResponse> getMyApplications(Long id);
}
