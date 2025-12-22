package com.secure.jobs.services.iml;

import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.JobApplicationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl  implements JobApplicationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;

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

        return jobApplicationRepository.save(app);
    }
}
