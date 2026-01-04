package com.secure.jobs.security.guards;

import com.secure.jobs.dto.profile.CandidateProfileResponse;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.models.user.profile.CandidateProfile;
import org.springframework.stereotype.Component;

@Component
public class ProfileCompletionGuard {

    public void assertComplete(CandidateProfileResponse profile) {
        boolean complete =
                profile.getResumeUrl() != null &&
                        profile.getEducationLevel() != null &&
                        profile.getDegreeField() != null &&
                        profile.getYearsExperience() != null;

        if (!complete) {
            throw new BadRequestException("Please complete your profile before applying.");
        }
    }
}
