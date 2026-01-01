package com.secure.jobs.dto.company;

import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.user.profile.DegreeField;
import com.secure.jobs.models.user.profile.EducationLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompanyJobApplicationRowResponse(
        Long applicationId,
        Long jobId,
        String jobTitle,
        Long applicantUserId,
        String applicantUsername,
        String applicantEmail,
        String resumeUrl,
        EducationLevel educationLevel,
        Long degreeFieldId,
        String degreeFieldName,
        Double yearsExperience,
        String location,
        JobApplicationStatus status,
        LocalDateTime appliedAt
) {
}
