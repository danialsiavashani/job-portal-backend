package com.secure.jobs.services;

import com.secure.jobs.dto.admin.UpdateCompanyEnabledResponse;
import com.secure.jobs.dto.company.CompanyProfileResponse;
import com.secure.jobs.models.company.Company;
import jakarta.validation.constraints.NotNull;

public interface CompanyService {
    CompanyProfileResponse getCompanyProfile(Long userId);

    UpdateCompanyEnabledResponse setEnabled(Long companyId, @NotNull Boolean enabled);

}
