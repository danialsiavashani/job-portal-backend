package com.secure.jobs.dto.profile;

import com.secure.jobs.models.user.profile.EducationLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CandidateProfileUpdateRequest {
    private EducationLevel educationLevel; // dropdown
    private Long degreeFieldId;            // dropdown (DegreeField)
    private BigDecimal yearsExperience;
    private String phone;
    private String location;
}
