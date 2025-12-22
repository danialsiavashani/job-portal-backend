package com.secure.jobs.repositories;

import com.secure.jobs.models.Job;
import com.secure.jobs.models.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    // =========================
    // Company dashboard jobs
    // =========================
    @Query("""
        SELECT j FROM Job j
        JOIN FETCH j.company
        WHERE j.company.id = :companyId
        ORDER BY j.createdAt DESC
    """)
    List<Job> findByCompanyIdWithCompany(Long companyId);

    // =========================
    // Single job (details page)
    // =========================
    @Query("""
        SELECT j FROM Job j
        JOIN FETCH j.company
        WHERE j.id = :id
    """)
    Optional<Job> findByIdWithCompany(Long id);

    // =========================
    // Public job feed
    // =========================
    @Query("""
        SELECT j FROM Job j
        JOIN FETCH j.company
        WHERE j.status = :status
        ORDER BY j.createdAt DESC
    """)
    List<Job> findByStatusWithCompany(JobStatus status);

    @Query("""
    SELECT j FROM Job j
    JOIN FETCH j.company
    WHERE j.status = :status
""")
    Page<Job> findByStatusWithCompany(
            JobStatus status,
            Pageable pageable
    );

}
