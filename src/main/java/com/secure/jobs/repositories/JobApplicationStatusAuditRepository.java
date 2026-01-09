package com.secure.jobs.repositories;

import com.secure.jobs.models.job.JobApplicationStatusAudits;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationStatusAuditRepository
        extends JpaRepository<JobApplicationStatusAudits, Long> {
}

