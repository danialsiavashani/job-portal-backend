package com.secure.jobs.services;

import com.secure.jobs.models.company.Company;

public interface CompanyService {
    Company getCompanyProfile(Long userId);
}
