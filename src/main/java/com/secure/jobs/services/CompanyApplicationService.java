package com.secure.jobs.services;

import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.models.company.CompanyApplication;

import java.util.List;

public interface CompanyApplicationService {
    CompanyApplication apply(Long userId, String companyName, String documentPublicId, String documentUrl);

    CompanyApplicationResponse approve(Long applicationId);

    CompanyApplicationResponse reject(Long applicationId, String reason);

    CompanyApplication findMyApplication(Long userId);

    List<CompanyJobApplicationRowResponse> getCompanyApplications(Long companyUserId, String status);
}
