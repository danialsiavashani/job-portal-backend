package com.secure.jobs.services.iml;

import com.secure.jobs.dto.profile.CandidateProfileResponse;
import com.secure.jobs.dto.profile.CandidateProfileResumeUpdateRequest;
import com.secure.jobs.dto.profile.CandidateProfileUpdateRequest;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.mappers.CandidateProfileMapper;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.models.user.profile.CandidateProfile;
import com.secure.jobs.models.user.profile.DegreeField;
import com.secure.jobs.repositories.CandidateProfileRepository;
import com.secure.jobs.repositories.DegreeFieldRepository;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.guards.JobApplicationGuard;
import com.secure.jobs.services.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final DegreeFieldRepository degreeFieldRepository;





    private CandidateProfile getOrCreateEntity(Long userId) {
        return candidateProfileRepository.findById(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            CandidateProfile profile = CandidateProfile.builder()
                    .user(user) // @MapsId sets userId
                    .build();

            return candidateProfileRepository.save(profile);
        });
    }


    @Override
    public CandidateProfileResponse getOrCreate(Long userId) {
        return CandidateProfileMapper.toResponse(getOrCreateEntity(userId));
    }


    @Override
    public CandidateProfileResponse update(Long userId, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = getOrCreateEntity(userId);

        boolean userHasActiveApplication = jobApplicationRepository.existsByUser_UserIdAndStatusIn(userId, JobApplicationGuard.ACTIVE_STATUSES);

        boolean tryingToChangeRestricted =
                request.getDegreeFieldId() != null
                        || request.getEducationLevel() != null
                        || request.getYearsExperience() != null; // optional

        if (userHasActiveApplication && tryingToChangeRestricted) {
            throw new ApiException(
                    "You can’t change degree/education/experience while you have active applications.",
                    HttpStatus.CONFLICT
            );
        }

        if (request.getEducationLevel() != null ) {
            profile.setEducationLevel(request.getEducationLevel());
        }
        if (request.getYearsExperience() != null) {
            profile.setYearsExperience(request.getYearsExperience());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getDegreeFieldId() != null) {
            DegreeField df = degreeFieldRepository.findById(request.getDegreeFieldId())
                    .orElseThrow(() -> new ApiException("DegreeField not found.", HttpStatus.NOT_FOUND));

            if (!df.isActive()) {
                throw new ApiException("DegreeField is inactive.", HttpStatus.CONFLICT);
            }

            profile.setDegreeField(df);
        }

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return CandidateProfileMapper.toResponse(saved);
    }


    @Override
    public CandidateProfileResponse updateResume(Long userId, CandidateProfileResumeUpdateRequest request) {
        CandidateProfile profile = getOrCreateEntity(userId);

        if (request.getResumeUrl() == null || request.getResumeUrl().isBlank()
                || request.getResumePublicId() == null || request.getResumePublicId().isBlank()) {
            throw new ApiException("resumeUrl and resumePublicId are required", HttpStatus.BAD_REQUEST);
        }

        profile.setResumeUrl(request.getResumeUrl());
        profile.setResumePublicId(request.getResumePublicId());

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return CandidateProfileMapper.toResponse(saved);
    }
}

