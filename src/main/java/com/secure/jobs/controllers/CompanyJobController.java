package com.secure.jobs.controllers;

import com.secure.jobs.dto.job.ChangeJobStatusRequest;
import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/jobs")
@RequiredArgsConstructor
public class CompanyJobController {

    private final JobService jobService;



    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public JobResponse createJob(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody @Valid CreateJobRequest request){
        Job job = jobService.createJob(user.getId(),request);
        return JobMapper.toResponse(job);
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public JobResponse updateJob(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long jobId,
            @RequestBody @Valid UpdateJobRequest request
    ) {
        Job job = jobService.updateJob(user.getId(), jobId, request);
        return JobMapper.toResponse(job);
    }

    @PatchMapping("/{jobId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> changeJobStatus(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long jobId,
            @RequestBody @Valid ChangeJobStatusRequest request
    ) {
        jobService.changeStatus(user.getId(), jobId, request.status());
        return ResponseEntity.ok("Job status updated successfully.");
    }

    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    public List<JobResponse> getMyJobs(
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return jobService.getJobsForCompany(user.getId());
    }


}
