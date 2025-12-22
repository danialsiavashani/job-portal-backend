package com.secure.jobs.controllers;

import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobController {

    private final JobService jobService;

    @GetMapping
    public JobPageResponse getPublishedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) BigDecimal minPay,
            @RequestParam(required = false) BigDecimal maxPay,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String location
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return jobService.getPublishedJobs(
                pageable,
                keyword,
                location,
                employmentType,
                minPay,
                maxPay,
                companyId
        );
    }

    @GetMapping("/{jobId}")
    public JobResponse getPublishedJobById(@PathVariable Long jobId) {
        return jobService.getPublishedJobById(jobId);
    }
}
