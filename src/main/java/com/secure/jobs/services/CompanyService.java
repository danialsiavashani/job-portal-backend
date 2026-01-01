package com.secure.jobs.services;

import com.secure.jobs.models.company.Company;
import jakarta.validation.constraints.NotNull;

public interface CompanyService {
    Company getCompanyProfile(Long userId);

    void setEnabled(Long companyId, @NotNull Boolean enabled);

}
