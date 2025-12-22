package com.secure.jobs.repositories;

import com.secure.jobs.models.job.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJob_IdAndUser_UserId(Long jobId, Long userId);

}
