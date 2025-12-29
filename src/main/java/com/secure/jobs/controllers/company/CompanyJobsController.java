package com.secure.jobs.controllers.company;

import com.secure.jobs.dto.company.CompanyJobApplicationPageResponse;
import com.secure.jobs.dto.job.*;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.CompanyApplicationService;
import com.secure.jobs.services.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/company/jobs")
@RequiredArgsConstructor
public class CompanyJobsController {

    private final JobService jobService;
    private final CompanyApplicationService companyApplicationService;

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

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> deleteJob(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable Long jobId){
        jobService.deleteJob(user.getId(), jobId);
       return ResponseEntity.ok("Job deleted successfully.");
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
    public JobPageResponse getMyJobs(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPay,
            @RequestParam(required = false) BigDecimal maxPay,
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
        return jobService.getJobsForCompany(
                user.getId(),
                locked,
                keyword,
                employmentType,
                status,
                location,
                minPay,
                maxPay,
                from,
                to
        );
    }


    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyJobApplicationPageResponse applicationPerJob(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @PathVariable Long jobId,
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
        return companyApplicationService.getCompanyApplicationsPerJob(
                user.getId(),
                locked,
                keyword,
                jobId,
                status,
                from,
                to);
    }




}
