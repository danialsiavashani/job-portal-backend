package com.secure.jobs.controllers.user;

import com.secure.jobs.dto.company.*;
import com.secure.jobs.mappers.CompanyApplicationMapper;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-applications")
@RequiredArgsConstructor
public class UserCompanyApplicationController {

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




}

