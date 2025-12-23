package com.secure.jobs.controllers;

import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public JobApplication apply(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long jobId,
            @RequestBody @Valid JobApplicationRequest request
    ) {
        return jobApplicationService.apply(user.getId(), jobId, request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<JobApplicationResponse> myApplications(
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return jobApplicationService.getMyApplications(user.getId());
    }


    @PutMapping("/{applicationId}/interview")
    public void moveToInterview(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long applicationId){
        jobApplicationService.moveToInterview(applicationId, userDetails.getId());
    }

    @PutMapping("/{applicationId}/reject")
    public void reject(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long applicationId){
        jobApplicationService.reject(applicationId, userDetails.getId());
    }

}
