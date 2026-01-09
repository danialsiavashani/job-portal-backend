package com.secure.jobs.controllers.user;


import com.secure.jobs.dto.job.JobPageResponse;
import com.secure.jobs.dto.job.SavedJobPageResponse;
import com.secure.jobs.dto.user.UserSavedJobResponse;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.security.services.UserDetailsImpl;
import com.secure.jobs.services.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/saved-jobs")
public class UserSavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public UserSavedJobResponse saveJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long jobId
            ){
        return savedJobService.saveJob(userDetails.getId(), jobId);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long jobId
    ){
        savedJobService.unSave(userDetails.getId(),jobId);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public SavedJobPageResponse getMySavedJobs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) BigDecimal minPay,
            @RequestParam(required = false) BigDecimal maxPay,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        Pageable locked = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return savedJobService.getMySavedJobs(
                userDetails.getId(),
                locked,
                keyword,
                location,
                employmentType,
                minPay,
                maxPay,
                companyId,
                from,
                to
        );
    }

    @GetMapping("/ids")
    @PreAuthorize("hasRole('USER')")
    public List<Long> getMySavedJobIds(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return savedJobService.getMySavedJobIds(userDetails.getId());
    }

    @GetMapping("/{jobId}/exists")
    @PreAuthorize("hasRole('USER')")
    public boolean isJobSaved(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long jobId
    ) {
        return savedJobService.isSaved(userDetails.getId(), jobId);
    }



}
