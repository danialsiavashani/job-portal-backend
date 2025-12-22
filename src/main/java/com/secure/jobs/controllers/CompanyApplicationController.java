package com.secure.jobs.controllers;

import com.secure.jobs.auth.dto.CompanyApplicationRequest;
import com.secure.jobs.auth.dto.CompanyApplicationResponse;
import com.secure.jobs.mappers.CompanyApplicationMapper;
import com.secure.jobs.models.CompanyApplication;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-applications")
@RequiredArgsConstructor
public class CompanyApplicationController {

    private final CompanyApplicationService companyApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public CompanyApplication apply(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CompanyApplicationRequest request
    ) {
        Long userId = userDetails.getId();

        return companyApplicationService.apply(
                userId,
                request.companyName(),
                request.documentPublicId(),
                request.documentUrl()
        );
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
    public CompanyApplication approve(@PathVariable Long id) {
        return companyApplicationService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CompanyApplication reject(@PathVariable Long id) {
        return companyApplicationService.reject(id, null);
    }

}
