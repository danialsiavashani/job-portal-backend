package com.secure.jobs.controllers;

import com.secure.jobs.dto.job.JobApplicationRequest;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

}
