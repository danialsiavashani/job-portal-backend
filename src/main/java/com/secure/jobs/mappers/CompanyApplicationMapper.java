package com.secure.jobs.mappers;

import com.secure.jobs.auth.dto.CompanyApplicationResponse;
import com.secure.jobs.models.CompanyApplication;

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
