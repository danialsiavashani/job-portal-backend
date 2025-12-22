package com.secure.jobs.mappers;

import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.models.company.CompanyApplication;

public class CompanyApplicationMapper {

    private CompanyApplicationMapper() {}

    public static CompanyApplicationResponse toResponse(
            CompanyApplication application
    ) {
        return new CompanyApplicationResponse(
                application.getId(),
                application.getCompanyName(),
                application.getStatus().name(),
                application.getDocumentUrl(),
                application.getCreatedAt()
        );
    }
}
