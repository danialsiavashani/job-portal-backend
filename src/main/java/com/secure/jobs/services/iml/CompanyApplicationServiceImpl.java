package com.secure.jobs.services.iml;

import com.secure.jobs.dto.admin.AdminCompanyApplicationPageResponse;
import com.secure.jobs.dto.admin.AdminCompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyApplicationResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationPageResponse;
import com.secure.jobs.dto.company.CompanyJobApplicationRowResponse;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.mappers.AdminCompanyApplicationMapper;
import com.secure.jobs.mappers.CompanyApplicationMapper;
import com.secure.jobs.mappers.CompanyJobApplicationMapper;
import com.secure.jobs.models.user.auth.AppRole;
import com.secure.jobs.models.user.auth.Role;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.company.CompanyApplication;
import com.secure.jobs.models.company.CompanyApplicationStatus;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.user.profile.EducationLevel;
import com.secure.jobs.repositories.*;
import com.secure.jobs.security.guards.JobGuard;
import com.secure.jobs.services.CompanyApplicationService;
import com.secure.jobs.specifications.AdminCompanyApplicationsSpecifications;
import com.secure.jobs.specifications.CompanyJobApplicationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CompanyApplicationServiceImpl implements CompanyApplicationService {

    private final CompanyApplicationRepository companyApplicationRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private  final JobGuard jobGuard;

    @Override
    @Transactional
    public CompanyApplicationResponse apply(
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
            CompanyApplication saved = companyApplicationRepository.save(existing);
            return CompanyApplicationMapper.toResponse(saved);
        }

        // First-time apply -> create a new row
        CompanyApplication application = CompanyApplication.builder()
                .user(user)
                .companyName(companyName)
                .documentPublicId(documentPublicId)
                .documentUrl(documentUrl)
                .status(CompanyApplicationStatus.PENDING)
                .build();
        CompanyApplication saved = companyApplicationRepository.save(application);
        return CompanyApplicationMapper.toResponse(saved);
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
    public AdminCompanyApplicationPageResponse getAdminCompanyApplications(
            Long userId,
            Pageable locked,
            String keyword,
            CompanyApplicationStatus status,
            LocalDate from,
            LocalDate to) {

        Specification<CompanyApplication> spec =
                Specification.where(AdminCompanyApplicationsSpecifications.keyword(keyword))
                        .and(AdminCompanyApplicationsSpecifications.createdBetween(from, to));

        if (status != null) {
            spec = spec.and(AdminCompanyApplicationsSpecifications.hasApplicationStatus(status));
        }

        Page<CompanyApplication> page = companyApplicationRepository.findAll(
                spec,
                locked
        );

        List<AdminCompanyApplicationResponse> adminCompanyApplications = page.getContent()
                .stream()
                .map(AdminCompanyApplicationMapper::toResponse)
                .toList();


        return new AdminCompanyApplicationPageResponse(
                adminCompanyApplications,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public CompanyApplicationResponse findMyApplication(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CompanyApplication app = companyApplicationRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No company application found for this user"
                ));
        return CompanyApplicationMapper.toResponse(app);
    }

    @Transactional(readOnly = true)
    @Override
    public CompanyJobApplicationPageResponse getCompanyApplications(
            Long companyUserId,
            Pageable locked,
            String keyword,
            JobApplicationStatus status,
            BigDecimal minYears,
            Long degreeFieldId,
            EducationLevel educationLevel,
            LocalDate from,
            LocalDate to
    ) {

        Specification<JobApplication> spec =
                Specification.where(CompanyJobApplicationSpecification.belongsToCompany(companyUserId))
                        .and(CompanyJobApplicationSpecification.keyword(keyword))
                        .and(CompanyJobApplicationSpecification.createdBetween(from, to))
                        .and(CompanyJobApplicationSpecification.minYearsExperience(minYears))
                        .and(CompanyJobApplicationSpecification.hasDegreeFieldId(degreeFieldId))
                        .and(CompanyJobApplicationSpecification.hasEducationLevel(educationLevel));



        if (status != null) {
            spec = spec.and(CompanyJobApplicationSpecification.hasApplicationStatus(status));
        }

        Page<JobApplication> page = jobApplicationRepository.findAll(
                spec,
                locked
        );

        List<CompanyJobApplicationRowResponse> jobApplications = page.getContent()
                .stream()
                .map(CompanyJobApplicationMapper::toResponse)
                .toList();


        return new CompanyJobApplicationPageResponse(
                jobApplications,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public CompanyJobApplicationPageResponse getCompanyApplicationsPerJob(
            Long companyUserId,
            Pageable locked,
            String keyword,
            Long jobId,
            JobApplicationStatus status,
            LocalDate from,
            LocalDate to
    ) {
        jobGuard.requireOwnedActiveCompanyJob(jobId, companyUserId);

        Specification<JobApplication> spec =
                Specification.where(CompanyJobApplicationSpecification.belongsToCompany(companyUserId))
                        .and(CompanyJobApplicationSpecification.belongsToSameJob(jobId))
                        .and(CompanyJobApplicationSpecification.keyword(keyword))
                        .and(CompanyJobApplicationSpecification.createdBetween(from, to));


        if (status != null) {
            spec = spec.and(CompanyJobApplicationSpecification.hasApplicationStatus(status));
        }

        Page<JobApplication> page = jobApplicationRepository.findAll(
                spec,
                locked
        );

        List<CompanyJobApplicationRowResponse> jobApplications = page.getContent()
                .stream()
                .map(CompanyJobApplicationMapper::toResponse)
                .toList();


        return new CompanyJobApplicationPageResponse(
                jobApplications,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }





}



