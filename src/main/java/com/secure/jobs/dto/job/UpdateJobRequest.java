package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.PayPeriod;
import com.secure.jobs.models.job.PayType;

import java.math.BigDecimal;
import java.util.List;

public record UpdateJobRequest(
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
        List<String> minimumRequirements
) {}

