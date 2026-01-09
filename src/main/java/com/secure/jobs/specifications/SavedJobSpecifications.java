package com.secure.jobs.specifications;


import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.models.job.SavedJob;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavedJobSpecifications {

    public static Specification<SavedJob> belongsToUser(Long userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction()
                        : cb.equal(root.get("user").get("userId"), userId);
    }

    public static Specification<SavedJob> jobIsPublished() {
        return (root, query, cb) ->
                cb.equal(root.join("job").get("status"), JobStatus.PUBLISHED);
    }

    public static Specification<SavedJob> companyEnabled() {
        return (root, query, cb) ->
                cb.isTrue(root.join("job").join("company").get("enabled"));
    }

    // ✅ now this is saved date filtering
    public static Specification<SavedJob> savedBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();

            Path<LocalDateTime> savedAt = root.get("createdAt");

            if (from != null && to != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(savedAt, from.atStartOfDay()),
                        cb.lessThan(savedAt, to.plusDays(1).atStartOfDay())
                );
            }
            if (from != null) return cb.greaterThanOrEqualTo(savedAt, from.atStartOfDay());
            return cb.lessThan(savedAt, to.plusDays(1).atStartOfDay());
        };
    }

    public static Specification<SavedJob> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";

            var job = root.join("job");
            var company = job.join("company");

            return cb.or(
                    cb.like(cb.lower(job.get("title")), like),
                    cb.like(cb.lower(job.get("description")), like),
                    cb.like(cb.lower(job.get("tagline")), like),
                    cb.like(cb.lower(company.get("name")), like)
            );
        };
    }
}
