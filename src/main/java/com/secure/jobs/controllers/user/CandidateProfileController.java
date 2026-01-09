package com.secure.jobs.controllers.user;

import com.secure.jobs.dto.profile.CandidateProfileResponse;
import com.secure.jobs.dto.profile.CandidateProfileResumeUpdateRequest;
import com.secure.jobs.dto.profile.CandidateProfileUpdateRequest;

import com.secure.jobs.mappers.CandidateProfileMapper;
import com.secure.jobs.security.services.UserDetailsImpl;

import com.secure.jobs.services.CandidateProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/profile")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public CandidateProfileResponse getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return candidateProfileService.getOrCreate(userDetails.getId());
    }

    @PatchMapping
    @PreAuthorize("hasRole('USER')")
    public CandidateProfileResponse updateMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CandidateProfileUpdateRequest request
    ) {
        return candidateProfileService.update(userDetails.getId(), request);
    }

    @PutMapping("/resume")
    @PreAuthorize("hasRole('USER')")
    public CandidateProfileResponse updateMyResume(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CandidateProfileResumeUpdateRequest request
    ) {
        return candidateProfileService.updateResume(userDetails.getId(), request);
    }
}

