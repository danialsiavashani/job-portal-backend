package com.secure.jobs.services;

import com.secure.jobs.models.auth.User;

public interface UserService {
    User getMe(Long id);
}
