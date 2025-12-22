package com.secure.jobs.services;

import com.secure.jobs.dto.CreateJobRequest;
import com.secure.jobs.dto.JobPageResponse;
import com.secure.jobs.dto.JobResponse;
import com.secure.jobs.dto.UpdateJobRequest;
import com.secure.jobs.models.EmploymentType;
import com.secure.jobs.models.Job;

import com.secure.jobs.models.JobStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface JobService {

    Job createJob(Long userId, CreateJobRequest request);

    Job updateJob(Long userId, Long jobId, UpdateJobRequest request);

    void deleteJob(Long userId, Long jobId);

    List<JobResponse> getJobsForCompany(Long userId);

    Job getJobById(Long jobId);

    JobPageResponse getPublishedJobs(
            Pageable pageable,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId
    );

    void changeStatus(Long userId, Long jobId, @NotNull JobStatus status);
}

