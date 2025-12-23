package com.secure.jobs.mappers;

import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.models.job.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class CompanyJobApplicationMapper {


    public CompanyJobApplicationRowResponse toRow(JobApplication app) {

        return new CompanyJobApplicationRowResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getUser().getUserId(),
                app.getUser().getUsername(),
                app.getUser().getEmail(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
