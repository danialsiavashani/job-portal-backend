package com.secure.jobs.services.iml;

import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.job.*;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.services.JobService;
import com.secure.jobs.specifications.JobSpecifications;
import com.secure.jobs.validation.JobPayValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobPayValidator jobPayValidator;


    @Override
    public Job createJob(Long userId, CreateJobRequest request) {
        Company company =  companyRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a company"));

        jobPayValidator.validate(request.payMin(), request.payMax(), request.payType(), request.payPeriod());
        Job job = JobMapper.toEntity(request, company);
        job.setStatus(JobStatus.DRAFT);

        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(Long userId, Long jobId, UpdateJobRequest request){

        // 1️⃣ Load job (with company)
        Job job = jobRepository.findByIdWithCompany(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // 2️⃣ Authorization: must own the company
        if (!job.getCompany().getOwner().getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to update this job");
        }

        // 3️⃣ Validate pay (merge existing + incoming)
        jobPayValidator.validateForUpdate(job, request);

        // 4️⃣ Apply updates
        JobMapper.updateEntity(job, request);

        // 5️⃣ Save and return
        return jobRepository.save(job);

    }

    @Override
    public void deleteJob(Long userId, Long jobId) {

    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsForCompany(Long userId) {

        Company company = companyRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a company"));

        return jobRepository.findByCompanyIdWithCompany(company.getId())
                .stream()
                .map(JobMapper::toResponse)
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public JobPageResponse getPublishedJobs(
            Pageable pageable,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId
    ) {


        Specification<Job> spec = JobSpecifications.isPublished();

        if (keyword != null) {
            spec = spec.and(JobSpecifications.keyword(keyword));
        }

        if (employmentType != null) {
            spec = spec.and(JobSpecifications.hasEmploymentType(employmentType));
        }

        if (companyId != null) {
            spec = spec.and(JobSpecifications.hasCompany(companyId));
        }

        if (location != null) {
            spec = spec.and(JobSpecifications.hasLocation(location));
        }

        if (minPay != null) {
            spec = spec.and(JobSpecifications.payMinAtLeast(minPay));
        }

        if (maxPay != null) {
            spec = spec.and(JobSpecifications.payMaxAtMost(maxPay));
        }

        Page<Job> page = jobRepository.findAll(
                spec,
                pageable
        );

        List<JobResponse> jobs = page.getContent()
                .stream()
                .map(JobMapper::toResponse)
                .toList();

        return new JobPageResponse(
                jobs,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public void changeStatus(Long userId, Long jobId, JobStatus newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Ownership check
        if (!job.getCompany().getOwner().getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this job");
        }

        // Allowed transitions only
        if (job.getStatus() == JobStatus.DRAFT && newStatus == JobStatus.PUBLISHED) {
            job.setStatus(JobStatus.PUBLISHED);
            return;
        }

        if (job.getStatus() == JobStatus.PUBLISHED && newStatus == JobStatus.DRAFT) {
            job.setStatus(JobStatus.DRAFT);
            return;
        }

        throw new BadRequestException(
                "Invalid status transition: " + job.getStatus() + " → " + newStatus
        );


    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getPublishedJobById(Long jobId) {

        Job job = jobRepository.findByIdWithCompany(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Job not found");
            // 👆 intentional: do NOT leak draft jobs
        }

        return JobMapper.toResponse(job);
    }
}
