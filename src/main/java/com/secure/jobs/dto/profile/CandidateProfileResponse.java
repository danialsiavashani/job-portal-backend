package com.secure.jobs.dto.profile;

import com.secure.jobs.models.user.profile.EducationLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CandidateProfileResponse {
    private Long userId;

    private String resumeUrl;

    private EducationLevel educationLevel;
    private DegreeFieldMiniResponse degreeField;

    private BigDecimal yearsExperience;
    private String phone;
    private String location;

    private boolean complete;

    @Data
    @Builder
    public static class DegreeFieldMiniResponse {
        private Long id;
        private String name;
    }
}
