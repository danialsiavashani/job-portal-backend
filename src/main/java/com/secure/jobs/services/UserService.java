package com.secure.jobs.services;

import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
import com.secure.jobs.dto.admin.UpdateUserModerationResponse;
import com.secure.jobs.models.user.auth.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface UserService {
    User getMe(Long id);

    UpdateUserModerationResponse patchModeration(Long userId, @Valid UpdateUserModerationRequest request);

    void generatePasswordResetToken(@NotBlank @Email String email);

    void resetPassword(@NotBlank String token, @NotBlank @Size(min = 8, max = 72) String s);
}
