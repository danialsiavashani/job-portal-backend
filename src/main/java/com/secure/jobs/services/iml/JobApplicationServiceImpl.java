package com.secure.jobs.services.iml;

import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.CompanyJobApplicationMapper;
import com.secure.jobs.mappers.JobApplicationMapper;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.guards.*;
import com.secure.jobs.services.CandidateProfileService;
import com.secure.jobs.services.JobApplicationService;
import com.secure.jobs.specifications.UserJobsApplicationsSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl  implements JobApplicationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyGuard companyGuard;
    private final JobApplicationGuard jobApplicationGuard;
    private final UserGuard userGuard;
    private final CandidateProfileService candidateProfileService;
    private final ProfileCompletionGuard profileCompletionGuard;
    private final DegreeEligibilityGuard degreeEligibilityGuard;



    @Override
    public JobApplicationResponse apply(Long userId, Long jobId) {

        Job job = jobRepository.findPublishedEnabledByIdWithCompanyAndDegreeFields(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ duplicate protection
        boolean alreadyApplied = jobApplicationRepository.existsByJob_IdAndUser_UserId(jobId, userId);
        if (alreadyApplied) {
            throw new ApiException("You already applied to this job.",HttpStatus.CONFLICT);

        }

        // ✅ profile must exist + be complete
        var profile = candidateProfileService.getOrCreate(userId);
        profileCompletionGuard.assertComplete(profile);
        degreeEligibilityGuard.assertEligible(profile, job);

        JobApplication app = JobApplication.builder()
                .job(job)
                .company(job.getCompany())   // derive from job
                .user(user)
                .status(JobApplicationStatus.PENDING)
                .build();

        job.incrementApplicants();
        JobApplication saved = jobApplicationRepository.save(app);
        return JobApplicationMapper.toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public JobApplicationPageResponse getMyApplications(
            Long userId,
            Pageable pageable,
            String keyword,
            JobApplicationStatus status,
            LocalDate from,
            LocalDate to
    ) {

        Specification<JobApplication> spec =
                Specification.where(UserJobsApplicationsSpecifications.belongsToUser(userId))
                        .and(UserJobsApplicationsSpecifications.keyword(keyword))
                        .and(UserJobsApplicationsSpecifications.createdBetween(from, to));

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

    @Override
    public JobApplication updateJobApplicationStatus(Long companyOwnerUserId, Long applicationId, JobApplicationStatus newStatus) {
        JobApplication application = jobApplicationGuard.requireCompanyOwnedEnabledPendingApplication(applicationId, companyOwnerUserId);

        if(newStatus != JobApplicationStatus.INTERVIEW &&
            newStatus != JobApplicationStatus.REJECTED &&
                newStatus != JobApplicationStatus.HIRED
        ){
            throw new  BadRequestException("Invalid status transition");
        }

        application.setStatus(newStatus);

        jobApplicationRepository.save(application);

        return application;
    }

    @Override
    public JobApplication withdrawFromJobApplication( Long applicationId, Long userId) {
        JobApplication app = userGuard.requireUserOwnedPendingApplication(applicationId, userId);
        app.setStatus(JobApplicationStatus.WITHDRAWN);
        return app;
    }

}
