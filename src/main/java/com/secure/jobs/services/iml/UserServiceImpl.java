package com.secure.jobs.services.iml;

import com.secure.jobs.dto.admin.UpdateUserModerationRequest;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;


    @Override
    public User getMe(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void patchModeration(Long userId, UpdateUserModerationRequest request) {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));
            boolean changed = false;

            if(request.getEnabled() != null){
                user.setEnabled(request.getEnabled());
                changed = true;
            }
        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
            changed = true;
        }
        if (!changed) {
            throw new ApiException("No moderation fields provided", HttpStatus.BAD_REQUEST);
        }

    }
}
