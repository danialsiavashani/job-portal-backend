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
public class UserGuard {

    private final JobApplicationRepository jobApplicationRepository;

    public  JobApplication requireUserOwnedPendingApplication(Long applicationId, Long userId){
        JobApplication app = jobApplicationRepository.findByIdAndUser_UserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if(app.getStatus() != JobApplicationStatus.PENDING){
            throw new ApiException("Only PENDING applications can be withdrawn", HttpStatus.BAD_REQUEST);
        }
        return app;
        }


    public JobApplication requireUserOwnedWithdrawApplication(Long applicationId, Long userId) {
        JobApplication app = jobApplicationRepository.findByIdAndUser_UserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (app.getStatus() != JobApplicationStatus.PENDING
                && app.getStatus() != JobApplicationStatus.INTERVIEW) {
            throw new ApiException(
                    "Only PENDING or INTERVIEW applications can be withdrawn",
                    HttpStatus.BAD_REQUEST
            );
        }

        return app;
    }


}

