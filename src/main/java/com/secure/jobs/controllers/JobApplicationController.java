package com.secure.jobs.controllers;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('USER')")
    public JobApplicationResponse apply(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long jobId,
            @RequestBody @Valid JobApplicationRequest request
    ) {
        JobApplication app = jobApplicationService.apply(user.getId(), jobId, request);
        return JobApplicationMapper.toResponse(app);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public JobApplicationPageResponse myApplications(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            JobApplicationStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return jobApplicationService.getMyApplications(
                user.getId(),
                pageable,
                keyword,
                status
        );
    }


    @PutMapping("/{applicationId}/interview")
    @PreAuthorize("hasRole('COMPANY')")
    public void moveToInterview(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long applicationId){
        jobApplicationService.moveToInterview(applicationId, userDetails.getId());
    }

    @PutMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('COMPANY')")
    public void reject(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long applicationId){
        jobApplicationService.reject(applicationId, userDetails.getId());
    }



}
