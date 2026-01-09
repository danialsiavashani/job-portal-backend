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

    public Job requredCompanyOwnedJob(Long jobId, Long companyOwnedUserId){
        return  jobRepository.findByIdAndCompany_Owner_UserId(jobId, companyOwnedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public Job requireOwnedActiveCompanyJob(Long jobId, Long companyOwnerUserId) {
        Job job = requredCompanyOwnedJob(jobId, companyOwnerUserId);
        companyGuard.requireEnabled(job.getCompany());
        return job;
    }
}
