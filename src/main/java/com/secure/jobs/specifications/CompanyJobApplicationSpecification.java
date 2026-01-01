package com.secure.jobs.specifications;

import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompanyJobApplicationSpecification {

    public static Specification<JobApplication> belongsToCompany(Long companyUserId) {
        return (root, query, cb) -> {
            var job = root.join("job");
            var company = job.join("company");
            var owner = company.join("owner");
            return cb.equal(owner.get("userId"), companyUserId);
        };
    }

    // Don’t join "job" twice in the same query if you can avoid it.
    // Right now belongsToCompany specs do root.join("job"). JPA usually handles it, but it can create duplicate joins in SQL
    // root.get("job").get("id") without a join
    public static Specification<JobApplication> belongsToSameJob(Long jobId) {
        return (root, query, cb) -> cb.equal(root.get("job").get("id"), jobId);
    }

    public static Specification<JobApplication> createdBetween(LocalDate from, LocalDate to) {
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

    public static Specification<JobApplication> minYearsExperience(BigDecimal minYears) {
        return (root, query, cb) -> {
            if (minYears == null) return cb.conjunction();
            var user = root.join("user");
            var profile = user.join("candidateProfile", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.greaterThanOrEqualTo(profile.get("yearsExperience"), minYears);
        };
    }

    public static Specification<JobApplication> hasEducationLevel(String level) {
        return (root, query, cb) -> {
            if (level == null || level.isBlank()) return cb.conjunction();
            var user = root.join("user");
            var profile = user.join("candidateProfile", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.equal(profile.get("educationLevel"), Enum.valueOf(com.secure.jobs.models.user.profile.EducationLevel.class, level));
        };
    }

    public static Specification<JobApplication> hasDegreeFieldId(Long degreeFieldId) {
        return (root, query, cb) -> {
            if (degreeFieldId == null) return cb.conjunction();
            var user = root.join("user");
            var profile = user.join("candidateProfile", jakarta.persistence.criteria.JoinType.LEFT);
            var degree = profile.join("degreeField", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.equal(degree.get("id"), degreeFieldId);
        };
    }


    public static Specification<JobApplication> hasApplicationStatus(JobApplicationStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();

            String like = "%" + keyword.toLowerCase() + "%";

            var job = root.join("job");
            var user = root.join("user");

            return cb.or(
                    cb.like(cb.lower(job.get("location")), like),
                    cb.like(cb.lower(job.get("title")), like),
                    cb.like(cb.lower(user.get("email")), like)
            );
        };
    }
}
