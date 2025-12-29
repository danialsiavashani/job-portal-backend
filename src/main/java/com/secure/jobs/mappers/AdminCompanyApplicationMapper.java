package com.secure.jobs.mappers;

import com.secure.jobs.dto.admin.AdminCompanyApplicationResponse;
import com.secure.jobs.models.company.CompanyApplication;

public class AdminCompanyApplicationMapper {

    public static AdminCompanyApplicationResponse toResponse(CompanyApplication app){
        return new AdminCompanyApplicationResponse(
                app.getId(),
                app.getUser().getUserId(),
                app.getUser().getUsername(),
                app.getUser().getEmail(),
                app.getDocumentUrl(),
                app.getCompanyName(),
                app.getStatus(),
                app.getCreatedAt()
        );

    }
}
