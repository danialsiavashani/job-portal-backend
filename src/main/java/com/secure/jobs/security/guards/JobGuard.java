package com.secure.jobs.security.guards;

import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobGuard {

    private final JobRepository jobRepository;
    private final CompanyGuard companyGuard;

    public Job requireOwnedActiveCompanyJob(Long userId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getCompany().getOwner().getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this job");
        }
        companyGuard.requireEnabled(job.getCompany());
        return job;
    }
}
