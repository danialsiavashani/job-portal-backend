package com.secure.jobs.controllers.open;

import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobsController {

    private final JobService jobService;

    @GetMapping
    public JobPageResponse getPublishedJobs(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) BigDecimal minPay,
            @RequestParam(required = false) BigDecimal maxPay,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String location,
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
        return jobService.getPublishedJobs(
                locked,
                keyword,
                location,
                employmentType,
                minPay,
                maxPay,
                companyId,
                from,
                to
        );
    }

    @GetMapping("/{jobId}")
    public JobResponse getPublishedJobById(@PathVariable Long jobId) {
        return jobService.getPublishedJobById(jobId);
    }

}
