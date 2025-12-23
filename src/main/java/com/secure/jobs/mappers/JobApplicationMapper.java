package com.secure.jobs.mappers;

import com.secure.jobs.dto.job.JobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;

public class JobApplicationMapper {

    private JobApplicationMapper() {}

    public static JobApplicationResponse toResponse(JobApplication app) {
        return new JobApplicationResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getJob().getCompany().getName(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
