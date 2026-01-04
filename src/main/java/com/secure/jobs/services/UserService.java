package com.secure.jobs.services;

import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
import com.secure.jobs.dto.admin.UpdateUserModerationResponse;
import com.secure.jobs.models.user.auth.User;
import jakarta.validation.Valid;

public interface UserService {
    User getMe(Long id);

    UpdateUserModerationResponse patchModeration(Long userId, @Valid UpdateUserModerationRequest request);
}
