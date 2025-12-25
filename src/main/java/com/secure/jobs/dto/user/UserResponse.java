package com.secure.jobs.dto.user;

import com.secure.jobs.models.company.CompanyApplicationStatus;

public record UserResponse (
         Long userId,
         String username,
         String email,
         String role,
         boolean enabled,
         CompanyApplicationStatus companyApplicationStatus

) { }
