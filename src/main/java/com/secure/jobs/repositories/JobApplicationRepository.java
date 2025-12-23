package com.secure.jobs.repositories;

import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJob_IdAndUser_UserId(Long jobId, Long userId);
    List<JobApplication> findAllByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"job", "user", "company"})
    List<JobApplication> findAllByCompany_IdOrderByCreatedAtDesc(Long companyId);

    @EntityGraph(attributePaths = {"job", "user", "company"})
    List<JobApplication> findAllByCompany_IdAndStatusOrderByCreatedAtDesc(
            Long companyId,
            JobApplicationStatus status
    );
}
