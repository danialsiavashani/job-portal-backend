package com.secure.jobs.validation;

import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.job.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class ValidateCompanyOwnership {


    public void validateCompanyOwnership(JobApplication application, Long companyUserId) {

        Long jobOwnerUserId =
                application.getJob()
                        .getCompany()
                        .getOwner()
                        .getUserId();

        if (!jobOwnerUserId.equals(companyUserId)) {
            throw new ResourceNotFoundException("You do not own this job");
        }
    }

}
