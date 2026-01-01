package com.secure.jobs.services;

import com.secure.jobs.dto.profile.CandidateProfileResumeUpdateRequest;
import com.secure.jobs.dto.profile.CandidateProfileUpdateRequest;
import com.secure.jobs.models.user.profile.CandidateProfile;

public interface CandidateProfileService {
    CandidateProfile getOrCreate(Long userId);
    CandidateProfile update(Long userId, CandidateProfileUpdateRequest request);
    CandidateProfile updateResume(Long userId, CandidateProfileResumeUpdateRequest request);
}
