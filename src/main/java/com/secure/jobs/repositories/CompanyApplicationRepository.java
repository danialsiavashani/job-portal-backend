package com.secure.jobs.repositories;

import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.job.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyApplicationRepository extends JpaRepository<CompanyApplication, Long> , JpaSpecificationExecutor<CompanyApplication> {
    boolean existsByUserAndStatus(User user, CompanyApplicationStatus companyApplicationStatus);
    Optional<CompanyApplication> findByUser(User user);
    Optional<CompanyApplication> findByUser_UserId(Long userId);

    @Query("select ca.status from CompanyApplication ca where ca.user.userId = :id")
    Optional<CompanyApplicationStatus> findStatusByUserId(Long id);

    @EntityGraph(
            attributePaths = {"user"},
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<CompanyApplication> findAll(Specification<CompanyApplication> spec, Pageable pageable);
}
