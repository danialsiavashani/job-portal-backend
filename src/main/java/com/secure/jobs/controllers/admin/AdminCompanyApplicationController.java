package com.secure.jobs.controllers.admin;

import com.secure.jobs.dto.admin.AdminCompanyApplicationPageResponse;
import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/company-applications")
@RequiredArgsConstructor
public class AdminCompanyApplicationController {

    private final CompanyApplicationService companyApplicationService;

    @GetMapping("")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdminCompanyApplicationPageResponse adminCompanyAuditApplications(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            CompanyApplicationStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
            ){
        Pageable locked = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return companyApplicationService.getAdminCompanyApplications(
                user.getId(),
                locked,
                keyword,
                status,
                from,
                to);
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

}
