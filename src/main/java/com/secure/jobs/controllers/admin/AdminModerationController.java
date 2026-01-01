package com.secure.jobs.controllers.admin;

import com.secure.jobs.dto.admin.UpdateCompanyEnabledRequest;
import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
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
    public ResponseEntity<Void> patchUserModeration(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserModerationRequest request
    ){
        userService.patchModeration(userId, request);
        return  ResponseEntity.noContent().build();
    }

    @PatchMapping("/companies/{companyId}/enabled")
    public ResponseEntity<Void> patchCompanyEnabled(
            @PathVariable Long companyId,
            @Valid @RequestBody UpdateCompanyEnabledRequest  request
            ){
        companyService.setEnabled(companyId, request.getEnabled());
        return ResponseEntity.noContent().build();
    }


}
