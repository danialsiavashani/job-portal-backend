package com.secure.jobs.services.iml;

import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.job.*;
import com.secure.jobs.models.user.profile.DegreeField;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.repositories.DegreeFieldRepository;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.security.guards.CompanyGuard;
import com.secure.jobs.security.guards.JobGuard;
import com.secure.jobs.services.CompanyService;
import com.secure.jobs.services.JobService;
import com.secure.jobs.specifications.CompanyJobsSpecifications;
import com.secure.jobs.specifications.JobSpecifications;
import com.secure.jobs.validation.JobPayValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobPayValidator jobPayValidator;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobGuard jobGuard;
    private final CompanyGuard companyGuard;
    private final CompanyRepository companyRepository;
    private final DegreeFieldRepository degreeFieldRepository;

    @Override
    public Job createJob(Long userId, CreateJobRequest request) {
        Company company = companyGuard.requireEnabledCompanyOwnedByUser(userId);
        jobPayValidator.validate(request.payMin(), request.payMax(), request.payType(), request.payPeriod());
        Set<DegreeField> degreeFields = resolveDegreeFields(request.degreeFieldIds());
        Job job = JobMapper.toEntity(request, company, degreeFields);
        job.setStatus(JobStatus.DRAFT);
        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(Long userId, Long jobId, UpdateJobRequest request){
        Job job = jobGuard.requireOwnedActiveCompanyJob(userId,jobId);
        jobPayValidator.validateForUpdate(job, request);
        Set<DegreeField> degreeFields = resolveDegreeFields(request.degreeFieldIds());
        JobMapper.updateEntity(job, request, degreeFields);
        return jobRepository.save(job);
    }

    @Override
    public void deleteJob(Long userId, Long jobId) {
        Job job = jobGuard.requireOwnedActiveCompanyJob(userId,jobId);
        if(jobApplicationRepository.existsByJob_Id(jobId)){
            throw new ApiException( "Cant delete a job, there are  applications associated with this job, better to disable it",HttpStatus.BAD_REQUEST);
        }
            jobRepository.delete(job);
    }



    @Override
    @Transactional(readOnly = true)
    public JobPageResponse getJobsForCompany(
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
    ) {
//        Company company = companyGuard.requireEnabledCompanyOwnedByUser(userId); // so i had to disable it, because I feel like no matter what a company should be able to see its jobs
        Company company = companyRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a company"));

        Specification<Job> spec =
                Specification.where(CompanyJobsSpecifications.belongsToCompany(company.getId()))
                        .and(CompanyJobsSpecifications.createdBetween(from, to));

        if (keyword != null) {
            spec = spec.and(CompanyJobsSpecifications.keyword(keyword));
        }

        if (employmentType != null) {
            spec = spec.and(CompanyJobsSpecifications.hasEmploymentType(employmentType));
        }

        if (status != null) {
            spec = spec.and(CompanyJobsSpecifications.hasJobStatus(status));
        }

        if (location != null) {
            spec = spec.and(CompanyJobsSpecifications.hasLocation(location));
        }

        if (minPay != null) {
            spec = spec.and(CompanyJobsSpecifications.payMinAtLeast(minPay));
        }

        if (maxPay != null) {
            spec = spec.and(CompanyJobsSpecifications.payMaxAtMost(maxPay));
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
    @Transactional(readOnly = true)
    public JobPageResponse getPublishedJobs(
            Pageable pageable,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId,
            LocalDate from,
            LocalDate to
    ) {

        Specification<Job> spec = JobSpecifications.isPublished()
                .and(JobSpecifications.createdBetween(from, to))
                .and(JobSpecifications.isCompanyEnabled());

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
        Job job = jobGuard.requireOwnedActiveCompanyJob(userId,jobId);
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
        Job job = jobRepository.findPublishedEnabledByIdWithCompanyAndDegreeFields(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return JobMapper.toResponse(job);
    }


    private Set<DegreeField> resolveDegreeFields(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();

        List<DegreeField> fields = degreeFieldRepository.findAllById(ids);

        Set<Long> foundIds = fields.stream().map(DegreeField::getId).collect(Collectors.toSet());
        Set<Long> missing = new HashSet<>(ids);
        missing.removeAll(foundIds);

        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("DegreeField(s) not found: " + missing);
        }

        for (DegreeField df : fields) {
            if (!df.isActive()) throw new BadRequestException("DegreeField is inactive: " + df.getId());
        }

        return new HashSet<>(fields);
    }


}
