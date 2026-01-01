package com.secure.jobs.services;

import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;

import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.job.JobStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;


public interface JobService {

    Job createJob(Long userId, CreateJobRequest request);

    Job updateJob(Long userId, Long jobId, UpdateJobRequest request);

    void deleteJob(Long userId, Long jobId);

    JobPageResponse getPublishedJobs(
            Pageable pageable,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId,
            LocalDate from,
            LocalDate to
    );

    JobPageResponse getJobsForCompany(
            Long userId,
            Pageable pageable,
            String keyword,
            EmploymentType employmentType,
            JobStatus status,
            String location,
            BigDecimal minPay,
            BigDecimal maxPay,
            LocalDate from,
            LocalDate to
    );

    void changeStatus(Long userId, Long jobId, @NotNull JobStatus status);

    JobResponse getPublishedJobById(Long jobId);
}

