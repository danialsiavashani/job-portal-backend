package com.secure.jobs.services;

import com.secure.jobs.dto.admin.AdminCompanyApplicationPageResponse;
import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationPageResponse;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.job.JobApplicationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;


public interface CompanyApplicationService {
    CompanyApplication apply(Long userId, String companyName, String documentPublicId, String documentUrl);

    CompanyApplicationResponse approve(Long applicationId);

    CompanyApplicationResponse reject(Long applicationId, String reason);

    CompanyApplication findMyApplication(Long userId);

    @Transactional(readOnly = true)
    CompanyJobApplicationPageResponse getCompanyApplications(
            Long companyUserId,
            Pageable locked,
            String keyword,
            JobApplicationStatus status,
            BigDecimal minYears,
            Long degreeFieldId,
            String educationLevel,
            LocalDate from,
            LocalDate to
    );

    CompanyJobApplicationPageResponse getCompanyApplicationsPerJob(Long companyUserId, Pageable locked, String keyword, Long jobId, JobApplicationStatus status, LocalDate from, LocalDate to);

    AdminCompanyApplicationPageResponse getAdminCompanyApplications(Long userId, Pageable locked, String keyword, CompanyApplicationStatus status, LocalDate from, LocalDate to);
}
