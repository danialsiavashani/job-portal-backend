package com.secure.jobs.security.guards;

import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.repositories.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobApplicationGuard {

    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyGuard companyGuard;

    public JobApplication requireCompanyOwnedApplication(Long applicationId, Long companyOwnerUserId) {
        return jobApplicationRepository
                .findByIdAndCompany_Owner_UserId(applicationId, companyOwnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public JobApplication requireCompanyOwnedEnabledPendingApplication(
            Long applicationId,
            Long companyOwnerUserId
    ) {
        JobApplication app = requireCompanyOwnedApplication(applicationId, companyOwnerUserId);

        companyGuard.requireEnabled(app.getCompany());

        if (app.getStatus() != JobApplicationStatus.PENDING) {
            throw new ApiException("Only PENDING applications can be updated", HttpStatus.BAD_REQUEST);
        }

        return app;
    }
}
