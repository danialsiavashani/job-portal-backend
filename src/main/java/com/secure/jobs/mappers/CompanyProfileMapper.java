package com.secure.jobs.mappers;

import com.secure.jobs.dto.company.CompanyProfileResponse;
import com.secure.jobs.models.company.Company;
import org.springframework.stereotype.Component;


public class CompanyProfileMapper {

    public  CompanyProfileMapper(){}

    public static CompanyProfileResponse toResponse(Company app){

        return new CompanyProfileResponse(
                app.getId(),
                app.getOwner().getUserId(),
                app.getOwner().getUsername(),
                app.getName(),
                app.isEnabled()
        );
    }
}
