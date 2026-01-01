package com.secure.jobs.mappers;

import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.models.job.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class CompanyJobApplicationMapper {


    public CompanyJobApplicationMapper(){}

    public static CompanyJobApplicationRowResponse toResponse(JobApplication app) {

        var profile = app.getUser().getCandidateProfile();

        Long degreeFieldId = null;
        String degreeFieldName = null;

        if (profile != null && profile.getDegreeField() != null) {
            degreeFieldId = profile.getDegreeField().getId();
            degreeFieldName = profile.getDegreeField().getName();
        }

        return new CompanyJobApplicationRowResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getUser().getUserId(),
                app.getUser().getUsername(),
                app.getUser().getEmail(),
                profile != null ? profile.getResumeUrl() : null,
                profile != null ? profile.getEducationLevel() : null,
                degreeFieldId,
                degreeFieldName,
                profile != null && profile.getYearsExperience() != null ? profile.getYearsExperience().doubleValue() : null,
                profile != null ? profile.getLocation() : null,
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
