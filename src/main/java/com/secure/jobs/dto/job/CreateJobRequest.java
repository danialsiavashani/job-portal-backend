package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.PayPeriod;
import com.secure.jobs.models.job.PayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record CreateJobRequest(

        // ── Required fields ──────────────────────────────────────────────────
        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be 255 characters or fewer")
        String title,

        @NotBlank(message = "description is required")
        @Size(max = 10_000, message = "description must be 10,000 characters or fewer")
        String description,

        @NotNull(message = "employmentType is required")
        EmploymentType employmentType,

        // ── Optional fields ──────────────────────────────────────────────────
        @Size(max = 255, message = "tagline must be 255 characters or fewer")
        String tagline,

        @Size(max = 255, message = "level must be 255 characters or fewer")
        String level,

        @PositiveOrZero(message = "payMin must be 0 or greater")
        BigDecimal payMin,

        @PositiveOrZero(message = "payMax must be 0 or greater")
        BigDecimal payMax,

        PayPeriod payPeriod,
        PayType payType,

        Set<Long> degreeFieldIds,

        @Size(max = 255, message = "location must be 255 characters or fewer")
        String location,

        List<String> benefits,
        List<String> minimumRequirements
) {}
