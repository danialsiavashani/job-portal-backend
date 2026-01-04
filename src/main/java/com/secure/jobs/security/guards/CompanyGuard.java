package com.secure.jobs.security.guards;


import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyGuard {

    private final CompanyRepository companyRepository;

    public Company requireCompanyOwnedByUser(Long userId) {
        // you already use this pattern a lot
        return companyRepository.findByOwner_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a company"));
    }

    public void requireEnabled(Company company) {
        if (company == null) throw new ResourceNotFoundException("Company not found");
        if (!company.isEnabled()) {
            throw new ApiException("Company is disabled", HttpStatus.FORBIDDEN);
        }
    }

    public Company requireEnabledCompanyOwnedByUser(Long userId) {
        Company company = requireCompanyOwnedByUser(userId);
        requireEnabled(company);
        return company;
    }

    public Company requireCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }
}
