package com.secure.jobs.security.guards;

import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.user.profile.CandidateProfile;
import com.secure.jobs.models.user.profile.DegreeField;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DegreeEligibilityGuard {

    public void assertEligible(CandidateProfile profile, Job job) {

        Set<DegreeField> allowed = job.getDegreeFields();

        // v1 decision: if job has no restrictions -> allow all
        if (allowed == null || allowed.isEmpty()) {
            return;
        }

        DegreeField candidateField = profile.getDegreeField();
        if (candidateField == null) {
            throw new BadRequestException("Your degree field must be set before applying.");
        }

        boolean match = allowed.stream()
                .anyMatch(df -> df.getId().equals(candidateField.getId()));

        if (!match) {
            throw new BadRequestException("You are not eligible to apply for this job based on degree field.");
        }
    }
}