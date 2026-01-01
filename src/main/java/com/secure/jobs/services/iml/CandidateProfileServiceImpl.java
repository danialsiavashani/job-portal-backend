package com.secure.jobs.services.iml;

import com.secure.jobs.dto.profile.CandidateProfileResumeUpdateRequest;
import com.secure.jobs.dto.profile.CandidateProfileUpdateRequest;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.models.user.profile.CandidateProfile;
import com.secure.jobs.models.user.profile.DegreeField;
import com.secure.jobs.repositories.CandidateProfileRepository;
import com.secure.jobs.repositories.DegreeFieldRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final DegreeFieldRepository degreeFieldRepository;


    @Override
    public CandidateProfile getOrCreate(Long userId) {
        return candidateProfileRepository.findById(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId)); // swap to your NotFound exception
            CandidateProfile profile = CandidateProfile.builder()
                    .user(user) // @MapsId will set userId automatically
                    .build();
            return candidateProfileRepository.save(profile);
        });
    }


    @Override
    public CandidateProfile update(Long userId, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = getOrCreate(userId);

        if (request.getEducationLevel() != null) {
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
                    .orElseThrow(() -> new RuntimeException("DegreeField not found: " + request.getDegreeFieldId()));
            if (!df.isActive()) {
                throw new RuntimeException("DegreeField is inactive: " + df.getId());
            }
            profile.setDegreeField(df);
        }

        return candidateProfileRepository.save(profile);
    }


    @Override
    public CandidateProfile updateResume(Long userId, CandidateProfileResumeUpdateRequest request) {
        CandidateProfile profile = getOrCreate(userId);

        if (request.getResumeUrl() == null || request.getResumeUrl().isBlank()
                || request.getResumePublicId() == null || request.getResumePublicId().isBlank()) {
            throw new IllegalArgumentException("resumeUrl and resumePublicId are required");
        }

        // later: if you want, delete old Cloudinary asset using old publicId before replacing
        profile.setResumeUrl(request.getResumeUrl());
        profile.setResumePublicId(request.getResumePublicId());

        return candidateProfileRepository.save(profile);
    }
}
