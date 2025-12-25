package com.secure.jobs.controllers;

import com.secure.jobs.dto.company.CompanyApplicationRequest;
import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.mappers.CompanyApplicationMapper;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-applications")
@RequiredArgsConstructor
public class CompanyApplicationController {

    private final CompanyApplicationService companyApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public CompanyApplicationResponse apply(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CompanyApplicationRequest request
    ) {
        Long userId = userDetails.getId();
        CompanyApplication app = companyApplicationService.apply(
                userId,
                request.companyName(),
                request.documentPublicId(),
                request.documentUrl()
        );
        return  CompanyApplicationMapper.toResponse(app);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY')")
    public CompanyApplicationResponse myApplication(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return CompanyApplicationMapper.toResponse(
                companyApplicationService.findMyApplication(
                        userDetails.getId()
                )
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CompanyApplicationResponse approve(@PathVariable Long id) {
        return companyApplicationService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CompanyApplicationResponse reject(@PathVariable Long id) {
        return companyApplicationService.reject(id, null);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('COMPANY')")
    public List<CompanyJobApplicationRowResponse> list(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "PENDING") String status
    ) {
        return companyApplicationService.getCompanyApplications(userDetails.getId(), status);
    }

}

