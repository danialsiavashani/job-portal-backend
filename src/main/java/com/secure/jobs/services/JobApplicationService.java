package com.secure.jobs.services;

import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.user.auth.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface JobApplicationService {
    JobApplicationResponse apply(Long userId, Long jobId);

    JobApplicationPageResponse getMyApplications(Long userId, Pageable pageable, String keyword, JobApplicationStatus status, LocalDate from, LocalDate to);

    JobApplication updateJobApplicationStatus(Long id, Long applicationId, @NotNull JobApplicationStatus status);

    JobApplication withdrawFromJobApplication(Long applicationId, Long userId);
}
