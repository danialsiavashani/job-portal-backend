package com.secure.jobs.controllers;

import com.secure.jobs.dto.JobPageResponse;
import com.secure.jobs.models.EmploymentType;
import com.secure.jobs.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/jobs")
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
}
