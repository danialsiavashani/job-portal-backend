package com.secure.jobs.controllers.company;

import com.secure.jobs.dto.company.CompanyProfileResponse;
import com.secure.jobs.mappers.CompanyProfileMapper;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/profile")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyProfileResponse getCompanyProfile(@AuthenticationPrincipal UserDetailsImpl user){
        Company company = companyService.getCompanyProfile(user.getId());
        return CompanyProfileMapper.toResponse(company);
    }

}
