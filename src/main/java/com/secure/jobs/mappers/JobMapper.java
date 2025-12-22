package com.secure.jobs.mappers;

import com.secure.jobs.dto.job.CreateJobRequest;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.UpdateJobRequest;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.job.Job;

public class JobMapper {

    public static Job toEntity(CreateJobRequest req, Company company) {
        return Job.builder()
                .title(req.title())
                .description(req.description())
                .tagline(req.tagline())
                .employmentType(req.employmentType())
                .level(req.level())

                .payMin(req.payMin())
                .payMax(req.payMax())
                .payPeriod(req.payPeriod())
                .payType(req.payType())

                .location(req.location())
                .benefits(req.benefits())
                .minimumRequirements(req.minimumRequirements())
                .company(company)
                .build();
    }

    public static JobResponse toResponse(Job job) {
        // safe if company is already loaded; otherwise fetch-join on read endpoints (see section 5)
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getTagline(),
                job.getEmploymentType(),
                job.getLevel(),

                job.getPayMin(),
                job.getPayMax(),
                job.getPayPeriod(),
                job.getPayType(),

                job.getLocation(),
                job.getBenefits(),
                job.getMinimumRequirements(),
                job.getStatus(),
                job.getCompany().getName(),
                job.getCreatedAt()
        );
    }

    public static void updateEntity(Job job, UpdateJobRequest req) {

        if (req.title() != null) job.setTitle(req.title());
        if (req.description() != null) job.setDescription(req.description());
        if (req.tagline() != null) job.setTagline(req.tagline());
        if (req.employmentType() != null) job.setEmploymentType(req.employmentType());
        if (req.level() != null) job.setLevel(req.level());
        if (req.payMin() != null) job.setPayMin(req.payMin());
        if (req.payMax() != null) job.setPayMax(req.payMax());
        if (req.payPeriod() != null) job.setPayPeriod(req.payPeriod());
        if (req.payType() != null) job.setPayType(req.payType());
        if (req.location() != null) job.setLocation(req.location());

        if (req.benefits() != null) {
            job.getBenefits().clear();
            job.getBenefits().addAll(req.benefits());
        }

        if (req.minimumRequirements() != null) {
            job.getMinimumRequirements().clear();
            job.getMinimumRequirements().addAll(req.minimumRequirements());
        }
    }
}


