package com.secure.jobs.services.iml;

import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyServiceIml implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public Company getCompanyProfile(Long userId) {

        return companyRepository.findByOwner_UserId(userId)
                .orElseThrow(()->new  ResourceNotFoundException("User not found"));
    }
}
