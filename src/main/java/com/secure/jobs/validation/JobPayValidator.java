package com.secure.jobs.validation;

import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.PayPeriod;
import com.secure.jobs.models.job.PayType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JobPayValidator {

    public void validate(
            BigDecimal payMin,
            BigDecimal payMax,
            PayType payType,
            PayPeriod payPeriod
    ) {
        boolean hasAnyPayField = payMin != null || payMax != null || payType != null || payPeriod != null;
        if (!hasAnyPayField) return;

        if (payType == null || payPeriod == null) {
            throw new BadRequestException("payType and payPeriod are required when providing pay info.");
        }

        if (payMin == null && payMax == null) {
            throw new BadRequestException("Provide payMin and/or payMax.");
        }

        if (payMin != null && payMin.signum() < 0)
            throw new BadRequestException("payMin cannot be negative.");

        if (payMax != null && payMax.signum() < 0)
            throw new BadRequestException("payMax cannot be negative.");

        if (payMin != null && payMax != null && payMax.compareTo(payMin) < 0)
            throw new BadRequestException("payMax must be >= payMin.");

        if (payPeriod == PayPeriod.HOUR && payType != PayType.HOURLY)
            throw new BadRequestException("If payPeriod is HOUR, payType must be HOURLY.");

        if (payType == PayType.SALARY && payPeriod == PayPeriod.HOUR)
            throw new BadRequestException("Salary cannot have payPeriod=HOUR.");
    }

    public void validateForUpdate(Job job, UpdateJobRequest req) {
        validate(
                req.payMin() != null ? req.payMin() : job.getPayMin(),
                req.payMax() != null ? req.payMax() : job.getPayMax(),
                req.payType() != null ? req.payType() : job.getPayType(),
                req.payPeriod() != null ? req.payPeriod() : job.getPayPeriod()
        );
    }
}

