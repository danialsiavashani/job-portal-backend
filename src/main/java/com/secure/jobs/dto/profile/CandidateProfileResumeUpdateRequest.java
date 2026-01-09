package com.secure.jobs.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CandidateProfileResumeUpdateRequest {
    private String resumeUrl;
    private String resumePublicId;
}
