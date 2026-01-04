package com.secure.jobs.controllers.admin;

import com.secure.jobs.dto.admin.UpdateCompanyEnabledRequest;
import com.secure.jobs.dto.admin.UpdateCompanyEnabledResponse;
import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
import com.secure.jobs.dto.admin.UpdateUserModerationResponse;
import com.secure.jobs.mappers.AdminModerationMapper;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.services.CompanyService;
import com.secure.jobs.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminModerationController {
    private final UserService userService;
    private final CompanyService companyService;


    @PatchMapping("/users/{userId}/moderation")
    public UpdateUserModerationResponse patchUserModeration(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserModerationRequest request
    ){
        return userService.patchModeration(userId, request);
    }

    @PatchMapping("/companies/{companyId}/enabled")
    public UpdateCompanyEnabledResponse patchCompanyEnabled(
            @PathVariable Long companyId,
            @Valid @RequestBody UpdateCompanyEnabledRequest  request
            ){
        return companyService.setEnabled(companyId, request.getEnabled());
    }

}
