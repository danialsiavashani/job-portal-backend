package com.secure.jobs.services.iml;

import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.JobApplicationMapper;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.JobApplicationService;
import com.secure.jobs.specifications.JobSpecifications;
import com.secure.jobs.specifications.UserJobsApplicationsSpecifications;
import com.secure.jobs.validation.ValidateCompanyOwnership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl  implements JobApplicationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ValidateCompanyOwnership validateOwnership;

    @Override
    public JobApplication apply(Long userId, Long jobId, JobApplicationRequest request) {

        Job job = jobRepository.findByIdWithCompany(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // ✅ only published jobs can be applied to
        if (job.getStatus() != JobStatus.PUBLISHED) {
            // return 404 so we don't leak draft jobs
            throw new ResourceNotFoundException("Job not found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ duplicate protection
        boolean alreadyApplied = jobApplicationRepository.existsByJob_IdAndUser_UserId(jobId, userId);
        if (alreadyApplied) {
            throw new BadRequestException("You already applied to this job.");
        }

        JobApplication app = JobApplication.builder()
                .job(job)
                .company(job.getCompany())   // derive from job
                .user(user)
                .status(JobApplicationStatus.PENDING)
                .documentPublicId(request.documentPublicId())
                .documentUrl(request.documentUrl())
                .build();

        job.incrementApplicants();

        return jobApplicationRepository.save(app);
    }

    @Override
    @Transactional
    public void moveToInterview(Long applicationId, Long companyUserId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        validateOwnership.validateCompanyOwnership(application, companyUserId);

        if (application.getStatus() != JobApplicationStatus.PENDING) {
            throw new BadRequestException("Application is already processed");
        }
        application.setStatus(JobApplicationStatus.INTERVIEW);
    }

    @Override
    @Transactional
    public void reject(Long applicationId, Long companyUserId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        validateOwnership.validateCompanyOwnership(application, companyUserId);

        if (application.getStatus() != JobApplicationStatus.PENDING) {
            throw new BadRequestException("Application is already processed");
        }

        application.setStatus(JobApplicationStatus.REJECTED);
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationPageResponse getMyApplications(Long userId, Pageable pageable,
                                                              String keyword, JobApplicationStatus status) {

        Specification<JobApplication> spec =
                Specification.where(UserJobsApplicationsSpecifications.belongsToUser(userId))
                        .and(UserJobsApplicationsSpecifications.keyword(keyword));

        if (status != null) {
            spec = spec.and(UserJobsApplicationsSpecifications.hasApplicationStatus(status));
        }

        Page<JobApplication> page = jobApplicationRepository.findAll(
                spec,
                pageable
        );

        List<JobApplicationResponse> jobApplications = page.getContent()
                .stream()
                .map(JobApplicationMapper::toResponse)
                .toList();



        return new JobApplicationPageResponse(
                jobApplications,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

}
