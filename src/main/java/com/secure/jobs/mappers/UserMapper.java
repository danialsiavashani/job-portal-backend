package com.secure.jobs.mappers;

import com.secure.jobs.dto.user.UserResponse;
import com.secure.jobs.dto.user.WithdrawJobApplicationResponse;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.models.company.CompanyApplicationStatus;

public class UserMapper {
    public static UserResponse toResponse(User user, CompanyApplicationStatus status){
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getRoleName().name(),
                user.isEnabled(),
                status

        );
    }

    public  static WithdrawJobApplicationResponse toWithdrawResponse(JobApplication app){
        return new WithdrawJobApplicationResponse(
                app.getUser().getUserId(),
                app.getStatus()
        );
    }
}
