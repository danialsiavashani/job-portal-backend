package com.secure.jobs.dto.job;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.PayPeriod;
import com.secure.jobs.models.job.PayType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record UpdateJobRequest(

        // All fields are optional: null means "do not change this field".
        // @Size(min=1) rejects an empty string when the field is explicitly provided,
        // while still allowing null (treated as no-op by the service layer).

        @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
        String title,

        @Size(min = 1, max = 10_000, message = "description must be between 1 and 10,000 characters")
        String description,

        @Size(max = 255, message = "tagline must be 255 characters or fewer")
        String tagline,

        EmploymentType employmentType,

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

