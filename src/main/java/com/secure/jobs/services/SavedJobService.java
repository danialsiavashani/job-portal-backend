package com.secure.jobs.services;

import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.SavedJobPageResponse;
import com.secure.jobs.dto.user.UserSavedJobResponse;
import com.secure.jobs.models.job.EmploymentType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SavedJobService {
    UserSavedJobResponse saveJob(Long userId, Long jobId);

    void unSave(Long userId,  Long jobId);

    SavedJobPageResponse getMySavedJobs(
            Long userId,
            Pageable locked,
            String keyword,
            String location,
            EmploymentType employmentType,
            BigDecimal minPay,
            BigDecimal maxPay,
            Long companyId,
            LocalDate from,
            LocalDate to);

    List<Long> getMySavedJobIds(Long id);

    boolean isSaved(Long id, Long jobId);
}
