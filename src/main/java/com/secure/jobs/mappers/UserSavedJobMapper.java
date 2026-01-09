package com.secure.jobs.mappers;

import com.secure.jobs.dto.user.UserSavedJobResponse;
import com.secure.jobs.models.job.SavedJob;

public class UserSavedJobMapper {

    public static UserSavedJobResponse toResponse(SavedJob savedJob){

        return new UserSavedJobResponse(
                savedJob.getId(),
                savedJob.getJob().getId()
        );
    }
}
