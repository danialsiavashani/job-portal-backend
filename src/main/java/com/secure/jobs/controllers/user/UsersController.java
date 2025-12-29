package com.secure.jobs.controllers.user;

import com.secure.jobs.dto.user.UserResponse;
import com.secure.jobs.mappers.UserMapper;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.repositories.CompanyApplicationRepository;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {


    private final UserService userService;
    private final CompanyApplicationRepository companyApplicationRepository;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getMyDetails(@AuthenticationPrincipal UserDetailsImpl userDetails){

        User  user = userService.getMe(userDetails.getId());

        CompanyApplicationStatus status =
                companyApplicationRepository.findStatusByUserId(userDetails.getId()).orElse(null);

        return UserMapper.toResponse(user, status);
    }
}
