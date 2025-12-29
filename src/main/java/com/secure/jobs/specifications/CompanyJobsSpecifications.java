package com.secure.jobs.specifications;

import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobStatus;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompanyJobsSpecifications {

    public static Specification<Job> belongsToCompany(Long companyId) {
        return (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<Job> createdBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();

            Path<LocalDateTime> createdAt = root.get("createdAt"); // LocalDateTime in your entity

            if (from != null && to != null) {
                LocalDateTime start = from.atStartOfDay();
                LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
                return cb.and(
                        cb.greaterThanOrEqualTo(createdAt, start),
                        cb.lessThan(createdAt, endExclusive)
                );
            }

            if (from != null) {
                return cb.greaterThanOrEqualTo(createdAt, from.atStartOfDay());
            }

            // to != null
            return cb.lessThan(createdAt, to.plusDays(1).atStartOfDay());
        };
    }

    public static Specification<Job> hasEmploymentType(EmploymentType type) {
        return (root, query, cb) ->
                cb.equal(root.get("employmentType"), type);
    }

    public static Specification<Job> hasJobStatus(JobStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"
                );
    }

    public static Specification<Job> payMinAtLeast(BigDecimal min) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("payMin"), min);
    }

    public static Specification<Job> payMaxAtMost(BigDecimal max) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("payMax"), max);
    }

    public static Specification<Job> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("tagline")), like)
            );
        };
    }

}
