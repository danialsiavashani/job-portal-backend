package com.secure.jobs.mappers;

import com.secure.jobs.dto.job.SavedJobResponse;
import com.secure.jobs.models.job.SavedJob;

public class SavedJobMapper {

    public static SavedJobResponse toResponse(SavedJob savedJob){

        return new SavedJobResponse(
                savedJob.getJob().getId(),
                savedJob.getJob().getTitle(),
                savedJob.getJob().getTagline(),
                savedJob.getJob().getEmploymentType(),
                savedJob.getJob().getLevel(),
                savedJob.getJob().getPayMin(),
                savedJob.getJob().getPayMax(),
                savedJob.getJob().getPayPeriod(),
                savedJob.getJob().getPayType(),
                savedJob.getJob().getLocation(),
                savedJob.getJob().getStatus(),
                savedJob.getJob().getCompany().getName()
        );
    }
}
