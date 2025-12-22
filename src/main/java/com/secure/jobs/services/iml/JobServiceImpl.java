package com.secure.jobs.services.iml;

import com.secure.jobs.dto.CreateJobRequest;
import com.secure.jobs.dto.JobPageResponse;
import com.secure.jobs.dto.JobResponse;
import com.secure.jobs.dto.UpdateJobRequest;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.models.*;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.services.JobService;
import com.secure.jobs.specifications.JobSpecifications;
import lombok.RequiredArgsConstructor;
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

    private void validatePay(BigDecimal payMin, BigDecimal payMax, PayType payType, PayPeriod payPeriod) {
        boolean hasAnyPayField = payMin != null || payMax != null || payType != null || payPeriod != null;

        // If they provided none of the pay fields, ok
        if (!hasAnyPayField) return;

        // If they provided any pay fields, require type + period
        if (payType == null || payPeriod == null) {
            throw new BadRequestException("payType and payPeriod are required when providing pay info.");
        }

        // Optional: require at least one amount
        if (payMin == null && payMax == null) {
            throw new BadRequestException("Provide payMin and/or payMax when payType/payPeriod are provided.");
        }

        if (payMin != null && payMin.signum() < 0) throw new BadRequestException("payMin cannot be negative.");
        if (payMax != null && payMax.signum() < 0) throw new BadRequestException("payMax cannot be negative.");
        if (payMin != null && payMax != null && payMax.compareTo(payMin) < 0) {
            throw new BadRequestException("payMax must be >= payMin.");
        }

        if (payPeriod == PayPeriod.HOUR && payType != PayType.HOURLY) {
            throw new BadRequestException("If payPeriod is HOUR, payType must be HOURLY.");
        }
        if (payType == PayType.SALARY && payPeriod == PayPeriod.HOUR) {
            throw new BadRequestException("Salary cannot have payPeriod=HOUR.");
        }
    }


    private void validatePayForUpdate(Job job, UpdateJobRequest req) {
        BigDecimal payMin   = (req.payMin()   != null) ? req.payMin()   : job.getPayMin();
        BigDecimal payMax   = (req.payMax()   != null) ? req.payMax()   : job.getPayMax();
        PayType payType     = (req.payType()  != null) ? req.payType()  : job.getPayType();
        PayPeriod payPeriod = (req.payPeriod()!= null) ? req.payPeriod(): job.getPayPeriod();

        validatePay(payMin, payMax, payType, payPeriod);
    }



    @Override
    public Job createJob(Long userId, CreateJobRequest request) {
        Company company =  companyRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a company"));

        validatePay(request.payMin(), request.payMax(), request.payType(), request.payPeriod());
        Job job = JobMapper.toEntity(request, company);
        job.setStatus(JobStatus.DRAFT);

        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(Long userId, Long jobId, UpdateJobRequest request) {
        return null;
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
    public Job getJobById(Long jobId) {
        return null;
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
}
