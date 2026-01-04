package com.secure.jobs.controllers.company;

import com.secure.jobs.dto.company.CompanyJobApplicationPageResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.dto.company.UpdateJobApplicationStatusRequest;
import com.secure.jobs.dto.job.UpdateJobApplicationStatusResponse;
import com.secure.jobs.mappers.JobApplicationMapper;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import com.secure.jobs.services.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company/job-applications")
public class CompanyJobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final CompanyApplicationService companyApplicationService;


    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public UpdateJobApplicationStatusResponse updateStatus(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateJobApplicationStatusRequest request
    ){
        JobApplication app = jobApplicationService.updateJobApplicationStatus(
            user.getId(),
            applicationId,
            request.status()
        );

        return JobApplicationMapper.toUpdateStatusResponse(app);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyJobApplicationPageResponse list(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            JobApplicationStatus status,
            @RequestParam(required = false)
            BigDecimal minYears,
            @RequestParam(required = false)
            Long degreeFieldId,
            @RequestParam(required = false)
            String educationLevel,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        Pageable locked = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return companyApplicationService.getCompanyApplications(
                user.getId(),
                locked,
                keyword,
                status,
                minYears,
                degreeFieldId,
                educationLevel,
                from,
                to);
    }

}
