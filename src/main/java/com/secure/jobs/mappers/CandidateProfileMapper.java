package com.secure.jobs.mappers;

import com.secure.jobs.dto.profile.CandidateProfileResponse;
import com.secure.jobs.models.user.profile.CandidateProfile;

public class CandidateProfileMapper {

    public static CandidateProfileResponse toResponse(CandidateProfile p) {
        return CandidateProfileResponse.builder()
                .userId(p.getUserId())
                .resumeUrl(p.getResumeUrl())
                .educationLevel(p.getEducationLevel())
                .degreeField(p.getDegreeField() == null ? null :
                        CandidateProfileResponse.DegreeFieldMiniResponse.builder()
                                .id(p.getDegreeField().getId())
                                .name(p.getDegreeField().getName())
                                .build()
                )
                .yearsExperience(p.getYearsExperience())
                .phone(p.getPhone())
                .location(p.getLocation())
                .complete(isComplete(p))
                .build();
    }

    public static boolean isComplete(CandidateProfile p) {
        return p.getResumeUrl() != null
                && p.getEducationLevel() != null
                && p.getDegreeField() != null
                && p.getYearsExperience() != null;
    }
}
