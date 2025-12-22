package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.models.job.PayPeriod;
import com.secure.jobs.models.job.PayType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(
        Long id,
        String title,
        String description,
        String tagline,
        EmploymentType employmentType,
        String level,

        BigDecimal payMin,
        BigDecimal payMax,
        PayPeriod payPeriod,
        PayType payType,

        String location,
        List<String> benefits,
        List<String> minimumRequirements,
        JobStatus status,
        String companyName,
        LocalDateTime createdAt
) {}
