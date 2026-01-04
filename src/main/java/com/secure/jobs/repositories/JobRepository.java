package com.secure.jobs.repositories;

import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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


    @EntityGraph(attributePaths = {
            "company",
            "degreeFields"
    })
    Page<Job> findAll(Specification<Job> spec, Pageable pageable);

    @Query("""
    select distinct j from Job j
    join fetch j.company c
    left join fetch j.degreeFields df
    where j.id = :jobId
      and j.status = com.secure.jobs.models.job.JobStatus.PUBLISHED
      and c.enabled = true
""")
    Optional<Job> findPublishedEnabledByIdWithCompanyAndDegreeFields(Long jobId);


}
