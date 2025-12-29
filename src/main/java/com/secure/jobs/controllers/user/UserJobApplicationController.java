package com.secure.jobs.controllers.user;

import com.secure.jobs.dto.job.JobApplicationPageResponse;
import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.mappers.JobApplicationMapper;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.security.services.UserDetailsImpl;
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

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserJobApplicationController {

    private final JobApplicationService jobApplicationService;


    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('USER')")
    public JobApplicationResponse apply(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long jobId,
            @RequestBody @Valid JobApplicationRequest request
    ) {
        JobApplication app = jobApplicationService.apply(user.getId(), jobId, request);
        return JobApplicationMapper.toResponse(app);
    }

    @GetMapping("/job-applications/me")
    @PreAuthorize("hasRole('USER')")
    public JobApplicationPageResponse myApplications(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            JobApplicationStatus status,
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
        return jobApplicationService.getMyApplications(
                user.getId(),
                locked,
                keyword,
                status,
                from,
                to
        );
    }
}
