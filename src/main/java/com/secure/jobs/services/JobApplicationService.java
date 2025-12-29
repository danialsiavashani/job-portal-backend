package com.secure.jobs.services;

import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface JobApplicationService {
    JobApplication apply(Long userId, Long jobId, JobApplicationRequest request);

    JobApplicationPageResponse getMyApplications(Long userId, Pageable pageable, String keyword, JobApplicationStatus status, LocalDate from, LocalDate to);

    CompanyJobApplicationRowResponse updateJobApplicationStatus(Long id, Long applicationId, @NotNull JobApplicationStatus status);
}
