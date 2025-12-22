package com.secure.jobs.services.iml;

import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.*;
import com.secure.jobs.repositories.CompanyApplicationRepository;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.services.CompanyApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyApplicationServiceImpl implements CompanyApplicationService {

    private final CompanyApplicationRepository companyApplicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public CompanyApplication apply(
            Long userId,
            String companyName,
            String documentPublicId,
            String documentUrl
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (companyApplicationRepository.existsByUserAndStatus(
                user,
                CompanyApplicationStatus.PENDING
        )) {
            throw new BadRequestException(
                    "You already have a pending company application"
            );
        }

        CompanyApplication application = CompanyApplication.builder()
                .user(user)
                .companyName(companyName)
                .documentPublicId(documentPublicId)
                .documentUrl(documentUrl)
                .status(CompanyApplicationStatus.PENDING)
                .build();

        return companyApplicationRepository.save(application);
    }


    @Override
    @Transactional
    public CompanyApplication approve(Long applicationId) {

        CompanyApplication application = companyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Company application not found"));

        if (application.getStatus() != CompanyApplicationStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending applications can be approved");
        }

        User user = application.getUser();

        // Create Company
        Company company = Company.builder()
                .name(application.getCompanyName())
                .owner(user)
                .build();

        companyRepository.save(company);

        // Grant ROLE_COMPANY
        Role companyRole = roleRepository.findByRoleName(AppRole.ROLE_COMPANY)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_COMPANY not found"));

        user.setRole(companyRole);
        userRepository.save(user);

        // Update application status
        application.setStatus(CompanyApplicationStatus.APPROVED);
        return companyApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public CompanyApplication reject(Long applicationId, String reason) {

        CompanyApplication application = companyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Company application not found"));

        if (application.getStatus() != CompanyApplicationStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending applications can be rejected");
        }

        application.setStatus(CompanyApplicationStatus.REJECTED);

        // optional: store reason later
        return companyApplicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyApplication findMyApplication(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return companyApplicationRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No company application found for this user"
                ));
    }
}
