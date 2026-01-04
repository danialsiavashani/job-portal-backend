package com.secure.jobs.services;

import com.secure.jobs.dto.profile.CandidateProfileResponse;
import com.secure.jobs.dto.profile.CandidateProfileResumeUpdateRequest;
import com.secure.jobs.dto.profile.CandidateProfileUpdateRequest;
import com.secure.jobs.models.user.profile.CandidateProfile;

public interface CandidateProfileService {
    CandidateProfileResponse getOrCreate(Long userId);
    CandidateProfileResponse update(Long userId, CandidateProfileUpdateRequest request);
    CandidateProfileResponse updateResume(Long userId, CandidateProfileResumeUpdateRequest request);
}
