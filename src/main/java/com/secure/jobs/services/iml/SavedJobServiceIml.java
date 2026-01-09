package com.secure.jobs.services.iml;

import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.JobResponse;
import com.secure.jobs.dto.job.SavedJobPageResponse;
import com.secure.jobs.dto.job.SavedJobResponse;
import com.secure.jobs.dto.user.UserSavedJobResponse;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.JobMapper;
import com.secure.jobs.mappers.SavedJobMapper;
import com.secure.jobs.mappers.UserSavedJobMapper;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.SavedJob;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.repositories.SavedJobRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.SavedJobService;
import com.secure.jobs.specifications.JobSpecifications;
import com.secure.jobs.specifications.SavedJobSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedJobServiceIml implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Override
    public UserSavedJobResponse saveJob(Long userId, Long jobId) {

        Optional<SavedJob> existing = savedJobRepository.findByUser_UserIdAndJob_Id(userId, jobId);
        if (existing.isPresent()) {
            throw new ApiException("You already have added this job to saved", HttpStatus.CONFLICT);
        }

        // 2) Load required entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // 3) Create + save
        SavedJob saved = savedJobRepository.save(SavedJob.builder()
                .user(user)
                .job(job)
                .build());

        return UserSavedJobMapper.toResponse(saved);
    }

    @Override
    public void unSave(Long userId, Long jobId) {
        savedJobRepository.deleteByUser_UserIdAndJob_Id(userId, jobId);
    }


    @Override
    @Transactional(readOnly = true)
    public SavedJobPageResponse getMySavedJobs(
            Long userId,
            Pageable locked,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId,
            LocalDate from,
            LocalDate to
    ) {
        Specification<SavedJob> spec = SavedJobSpecifications.belongsToUser(userId)
                .and(SavedJobSpecifications.savedBetween(from, to))
                .and(SavedJobSpecifications.jobIsPublished())
                .and(SavedJobSpecifications.companyEnabled());

        if (keyword != null) {
            spec = spec.and(SavedJobSpecifications.keyword(keyword));
        }



        Page<SavedJob> page = savedJobRepository.findAll(
                spec,
                locked
        );

        List<SavedJobResponse> jobs = page.getContent()
                .stream()
                .map(SavedJobMapper::toResponse)
                .toList();

        return new SavedJobPageResponse(
                jobs,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMySavedJobIds(Long userId) {
        return savedJobRepository.findSavedJobIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSaved(Long userId, Long jobId) {
        return savedJobRepository.existsByUser_UserIdAndJob_Id(userId, jobId);
    }
}
