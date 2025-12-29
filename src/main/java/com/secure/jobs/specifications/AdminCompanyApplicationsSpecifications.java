package com.secure.jobs.specifications;

import com.secure.jobs.models.auth.AppRole;
import com.secure.jobs.models.auth.Role;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import jakarta.persistence.criteria.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminCompanyApplicationsSpecifications {


    public static Specification<CompanyApplication> createdBetween(LocalDate from, LocalDate to) {
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


    public static Specification<CompanyApplication> hasApplicationStatus(CompanyApplicationStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }


    public static Specification<CompanyApplication> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();

            String like = "%" + keyword.toLowerCase() + "%";

            var user = root.join("user");

            return cb.or(
                    cb.like(cb.lower(user.get("email")), like)
            );
        };
    }
}
