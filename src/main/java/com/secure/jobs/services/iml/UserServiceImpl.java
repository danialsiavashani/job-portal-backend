package com.secure.jobs.services.iml;

import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.repositories.CompanyApplicationRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
}
