package com.secure.jobs.specifications;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class JobSpecifications {

    public static Specification<Job> isPublished() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), JobStatus.PUBLISHED);
    }

    public static Specification<Job> hasCompany(Long companyId) {
        return (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<Job> hasEmploymentType(EmploymentType type) {
        return (root, query, cb) ->
                cb.equal(root.get("employmentType"), type);
    }

    public static Specification<Job> payMinAtLeast(BigDecimal min) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("payMin"), min);
    }

    public static Specification<Job> payMaxAtMost(BigDecimal max) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("payMax"), max);
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"
                );
    }

    public static Specification<Job> keyword(String keyword) {
        return (root, query, cb) -> {
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("tagline")), like)
            );
        };
    }
}
