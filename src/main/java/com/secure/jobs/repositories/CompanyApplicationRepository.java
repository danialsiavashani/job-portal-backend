package com.secure.jobs.repositories;

import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyApplicationRepository extends JpaRepository<CompanyApplication, Long> {
    boolean existsByUserAndStatus(User user, CompanyApplicationStatus companyApplicationStatus);
    Optional<CompanyApplication> findByUser(User user);
    Optional<CompanyApplication> findByUser_UserId(Long userId);

}
