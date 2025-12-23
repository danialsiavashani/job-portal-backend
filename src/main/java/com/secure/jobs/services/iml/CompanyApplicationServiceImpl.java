package com.secure.jobs.services.iml;

import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.CompanyApplicationMapper;
import com.secure.jobs.mappers.CompanyJobApplicationMapper;
import com.secure.jobs.models.auth.AppRole;
import com.secure.jobs.models.auth.Role;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.repositories.*;
import com.secure.jobs.services.CompanyApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyApplicationServiceImpl implements CompanyApplicationService {

    private final CompanyApplicationRepository companyApplicationRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyJobApplicationMapper companyJobApplicationMapper;
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

        CompanyApplication existing =
                companyApplicationRepository.findByUser_UserId(userId).orElse(null);

        // If user already has an application row (because user_id is UNIQUE)
        if (existing != null) {

            if (existing.getStatus() == CompanyApplicationStatus.PENDING) {
                throw new BadRequestException("You already have a pending company application");
            }

            if (existing.getStatus() == CompanyApplicationStatus.APPROVED) {
                throw new BadRequestException("Your company application is already approved");
            }

            // REJECTED -> reset and reuse the SAME row
            existing.setCompanyName(companyName);
            existing.setDocumentPublicId(documentPublicId);
            existing.setDocumentUrl(documentUrl);
            existing.setStatus(CompanyApplicationStatus.PENDING);

            return companyApplicationRepository.save(existing);
        }

        // First-time apply -> create a new row
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
    public CompanyApplicationResponse approve(Long applicationId) {

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
        companyApplicationRepository.save(application);
        return CompanyApplicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public CompanyApplicationResponse reject(Long applicationId, String reason) {

        CompanyApplication application = companyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Company application not found"));

        if (application.getStatus() != CompanyApplicationStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending applications can be rejected");
        }

        application.setStatus(CompanyApplicationStatus.REJECTED);

        companyApplicationRepository.save(application);

        return CompanyApplicationMapper.toResponse(application);
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

    @Override
    @Transactional(readOnly = true)
    public List<CompanyJobApplicationRowResponse> getCompanyApplications(Long companyUserId, String statusParam) {
        Company company = companyRepository.findByOwner_UserId(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for user"));

        Long companyId = company.getId();

        String normalized = (statusParam == null ? "PENDING" : statusParam.trim().toUpperCase());

        List<JobApplication> apps;

        if ("ALL".equals(normalized)) {
            apps = jobApplicationRepository.findAllByCompany_IdOrderByCreatedAtDesc(companyId);
        } else {
            JobApplicationStatus status;
            try {
                status = JobApplicationStatus.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid status. Use ALL, PENDING, INTERVIEW, or REJECTED.");
            }
            apps = jobApplicationRepository.findAllByCompany_IdAndStatusOrderByCreatedAtDesc(companyId, status);
        }

        return apps.stream()
                .map(companyJobApplicationMapper::toRow)
                .toList();
    }

    }



