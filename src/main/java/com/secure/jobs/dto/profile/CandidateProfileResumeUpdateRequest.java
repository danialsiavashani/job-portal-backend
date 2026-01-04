package com.secure.jobs.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CandidateProfileResumeUpdateRequest {
    @NotBlank
    private String resumeUrl;

    @NotBlank
    private String resumePublicId;
}
