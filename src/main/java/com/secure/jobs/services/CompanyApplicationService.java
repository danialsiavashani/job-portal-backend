package com.secure.jobs.services;

import com.secure.jobs.models.CompanyApplication;

public interface CompanyApplicationService {
    CompanyApplication apply(Long userId, String companyName, String documentPublicId, String documentUrl);

    CompanyApplication approve(Long applicationId);

    CompanyApplication reject(Long applicationId, String reason);

    CompanyApplication findMyApplication(Long userId);
}
