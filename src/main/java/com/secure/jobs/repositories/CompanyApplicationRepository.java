package com.secure.jobs.repositories;

import com.secure.jobs.models.CompanyApplication;
import com.secure.jobs.models.CompanyApplicationStatus;
import com.secure.jobs.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyApplicationRepository extends JpaRepository<CompanyApplication, Long> {
    boolean existsByUserAndStatus(User user, CompanyApplicationStatus companyApplicationStatus);
    Optional<CompanyApplication> findByUser(User user);
}
