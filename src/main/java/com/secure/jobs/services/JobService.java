package com.secure.jobs.services;

import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;

import com.secure.jobs.models.job.JobStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface JobService {

    Job createJob(Long userId, CreateJobRequest request);

    Job updateJob(Long userId, Long jobId, UpdateJobRequest request);

    void deleteJob(Long userId, Long jobId);

    List<JobResponse> getJobsForCompany(Long userId);
    

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

    JobResponse getPublishedJobById(Long jobId);
}

