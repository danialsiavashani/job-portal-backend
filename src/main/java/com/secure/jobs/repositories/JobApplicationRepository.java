package com.secure.jobs.repositories;

import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> , JpaSpecificationExecutor<JobApplication> {
    boolean existsByJob_IdAndUser_UserId(Long jobId, Long userId);

    // OPTIONAL but recommended for /me list so mapper has job+company without N+1:

    @EntityGraph(
            attributePaths = {"job", "job.company", "user"},
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<JobApplication> findAll(Specification<JobApplication> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "user", "company"})
    Optional<JobApplication> findByIdAndCompany_Owner_UserId(Long id, Long ownerUserId);


    @EntityGraph(attributePaths = {"job", "user", "company"})
    List<JobApplication> findAllByCompany_IdOrderByCreatedAtDesc(Long companyId);

    @EntityGraph(attributePaths = {"job", "user", "company"})
    List<JobApplication> findAllByCompany_IdAndStatusOrderByCreatedAtDesc(
            Long companyId,
            JobApplicationStatus status
    );

    boolean existsByJob_Id(Long jobId);


    Optional<JobApplication> findByIdAndUser_UserId(
            @Param("userId") Long userId,
            @Param("applicationId") Long applicationId
    );
}
