package com.secure.jobs.services;

import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobApplicationService {
    JobApplication apply(Long userId, Long jobId, JobApplicationRequest request);

    void moveToInterview(Long applicationId, Long companyUserId);

    void reject(Long applicationId, Long companyUserId);

    JobApplicationPageResponse getMyApplications(Long userId, Pageable pageable, String keyword, JobApplicationStatus status);
}
