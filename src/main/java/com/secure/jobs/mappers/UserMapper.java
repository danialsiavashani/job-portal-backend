package com.secure.jobs.mappers;

import com.secure.jobs.dto.user.UserResponse;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.company.CompanyApplication;
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
}
