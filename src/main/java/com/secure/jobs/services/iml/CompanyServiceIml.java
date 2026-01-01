package com.secure.jobs.services.iml;

import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.security.guards.CompanyGuard;
import com.secure.jobs.services.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceIml implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyGuard companyGuard;


    @Override
    public Company getCompanyProfile(Long userId) {

        return companyRepository.findByOwner_UserId(userId)
                .orElseThrow(()->new  ResourceNotFoundException("User not found"));
    }

    @Override
    public void setEnabled(Long companyId, Boolean enabled) {
        Company company = companyGuard.requireCompanyById(companyId);
        company.setEnabled(enabled);
    }
}
