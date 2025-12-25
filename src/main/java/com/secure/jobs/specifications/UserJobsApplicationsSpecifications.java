package com.secure.jobs.specifications;


import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

public class UserJobsApplicationsSpecifications {

    public static Specification<JobApplication> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("userId"), userId);
    }

    public static Specification<JobApplication> hasApplicationStatus(JobApplicationStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();

            String like = "%" + keyword.toLowerCase() + "%";

            var companyJoin = root.join("company");
            var locationJoin = root.join("job");
            var titleJoin = root.join("job");

            return cb.or(
                    cb.like(cb.lower(companyJoin.get("name")), like),
                    cb.like(cb.lower(locationJoin.get("location")), like),
                    cb.like(cb.lower(titleJoin.get("title")), like)
            );
        };
    }
}
