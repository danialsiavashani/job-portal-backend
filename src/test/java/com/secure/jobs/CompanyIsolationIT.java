package com.secure.jobs;

import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.job.EmploymentType;
import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.JobApplication;
import com.secure.jobs.models.job.JobApplicationStatus;
import com.secure.jobs.models.job.JobStatus;
import com.secure.jobs.models.user.auth.AppRole;
import com.secure.jobs.models.user.auth.Role;
import com.secure.jobs.models.user.auth.User;
import com.secure.jobs.repositories.CompanyRepository;
import com.secure.jobs.repositories.JobApplicationRepository;
import com.secure.jobs.repositories.JobRepository;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;






import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompanyIsolationIT {

    // ✅ MUST match your controllers' class-level @RequestMapping
    private static final String BASE_APP = "/api/company/job-applications";
    private static final String BASE_JOB = "/api/company/jobs";

    @Autowired MockMvc mockMvc;

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired JobRepository jobRepository;
    @Autowired JobApplicationRepository jobApplicationRepository;


    private record CompanyCtx(User owner, Company company, RequestPostProcessor auth) {}
    private record JobCtx(CompanyCtx companyCtx, Job job) {}


    private String uniqueId() {
        return String.valueOf(System.nanoTime());
    }

    private String randomEmail() {
        return "u" + uniqueId() + "@test.com";
    }

    private String randomUsername(String prefix) {
        return prefix + "_" + uniqueId();
    }


    private CompanyCtx companyCtx(String companyName) {
        Role companyRole = ensureRole(AppRole.ROLE_COMPANY);

        User owner = newUser(
                randomUsername("owner"),
                randomEmail(),
                companyRole
        );

        Company company = newCompany(companyName, owner);

        return new CompanyCtx(owner, company, companyPrincipal(owner));
    }


    private JobCtx jobCtx(CompanyCtx ctx, String title) {
        Job job = jobRepository.save(Job.builder()
                .company(ctx.company())
                .title(title)
                .description("desc")
                .employmentType(EmploymentType.FULL_TIME)
                .status(JobStatus.PUBLISHED)
                .build());

        return new JobCtx(ctx, job);
    }

    private User candidate(String prefix) {
        Role userRole = ensureRole(AppRole.ROLE_USER);
        String u = prefix + "_" + System.nanoTime();
        return newUser(u, u + "@test.com", userRole);
    }

    private void forceAppliedAt(JobApplication app, LocalDateTime when) {
        // Try common field names used for date filtering
        List<String> candidates = List.of("createdAt", "appliedAt");

        for (String field : candidates) {
            try {
                ReflectionTestUtils.setField(app, field, when);
                return; // success
            } catch (IllegalArgumentException ignored) {
                // field not found or type mismatch -> try next
            }
        }

        throw new IllegalStateException(
                "Could not set applied/created timestamp on JobApplication. " +
                        "Update forceAppliedAt() with your actual field name (e.g., createdAt/appliedAt)."
        );
    }





    private Role ensureRole(AppRole roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
    }

    private User newUser(String username, String email, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash("x")
                .role(role)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .twoFactorEnabled(false)
                .build());
    }

    private Company newCompany(String name, User owner) {
        return companyRepository.save(Company.builder()
                .name(name)
                .owner(owner)
                .enabled(true)
                .build());
    }

    private Job newJob(Company company) {
        return jobRepository.save(Job.builder()
                .company(company)
                .title("Backend Dev")
                .description("desc")
                .employmentType(EmploymentType.FULL_TIME)
                .status(JobStatus.PUBLISHED)
                .build());
    }

    private JobApplication newPendingApplication(Company company, Job job, User candidateUser) {
        // ✅ user is required (your schema has NOT NULL user_id)
        return jobApplicationRepository.save(JobApplication.builder()
                .company(company)
                .job(job)
                .user(candidateUser)
                .status(JobApplicationStatus.PENDING)
                .build());
    }


    private RequestPostProcessor userPrincipal(User user) {
        UserDetailsImpl principal = UserDetailsImpl.build(user);

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }

//    Avoid duplicate usernames across tests
    private RequestPostProcessor asUser() {
        String u = "user_" + System.nanoTime();
        Role userRole = ensureRole(AppRole.ROLE_USER);
        User user = newUser(u, u + "@test.com", userRole);
        return userPrincipal(user);
    }




    private RequestPostProcessor companyPrincipal(User owner) {
        UserDetailsImpl principal = UserDetailsImpl.build(owner);

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_COMPANY"))
        );

        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }

    @Test
    void userRole_hittingCompanyEndpointToGetJobApplications_shouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_APP + "/list").with(asUser()))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRole_hittingCompanyEndpointToGetCompanyJobs_shouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_JOB).with(asUser()))
                .andExpect(status().isForbidden());
    }


    @Test
    void companyA_cannotUpdate_companyB_applicationStatus_returns404_andDoesNotChangeStatus() throws Exception {
        Role companyRole = ensureRole(AppRole.ROLE_COMPANY);
        Role userRole = ensureRole(AppRole.ROLE_USER);

        // Company A
        User ownerA = newUser("ownerA", "a@test.com", companyRole);
        newCompany("A Inc", ownerA);

        // Company B
        User ownerB = newUser("ownerB", "b@test.com", companyRole);
        Company companyB = newCompany("B Inc", ownerB);

        // Candidate user (the applicant)
        User candidate = newUser("cand1", "cand@test.com", userRole);

        Job jobB = newJob(companyB);
        JobApplication appB = newPendingApplication(companyB, jobB, candidate);

        JobApplicationStatus original = appB.getStatus();

        mockMvc.perform(
                        patch(BASE_APP + "/" + appB.getId() + "/status")
                                .with(companyPrincipal(ownerA))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"REJECTED\"}")
                )
                .andExpect(status().isNotFound());

        JobApplication reloaded = jobApplicationRepository.findById(appB.getId()).orElseThrow();
        Assertions.assertEquals(original, reloaded.getStatus());
    }

    @Test
    void companyA_cannotRead_companyB_jobApplications_returns404() throws Exception {
        Role companyRole = ensureRole(AppRole.ROLE_COMPANY);

        // Company A
        User ownerA = newUser("ownerA2", "a2@test.com", companyRole);
        newCompany("A2 Inc", ownerA);

        // Company B
        User ownerB = newUser("ownerB2", "b2@test.com", companyRole);
        Company companyB = newCompany("B2 Inc", ownerB);

        Job jobB = newJob(companyB);

        mockMvc.perform(
                        get(BASE_JOB + "/" + jobB.getId() + "/applications")
                                .with(companyPrincipal(ownerA))
                                .param("page", "0")
                                .param("size", "10")
                                .param("from", LocalDate.now().minusYears(1).toString())
                                .param("to", LocalDate.now().plusDays(1).toString())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void companyB_canRead_itsOwn_jobApplications_returns200_withExpectedRows() throws Exception {
        CompanyCtx companyB = companyCtx("B Inc Pos");
        JobCtx jobB = jobCtx(companyB, "Backend Dev B");

        User cand1 = candidate("cand1");
        User cand2 = candidate("cand2");

        JobApplication app1 = newPendingApplication(companyB.company(), jobB.job(), cand1);
        JobApplication app2 = newPendingApplication(companyB.company(), jobB.job(), cand2);

        mockMvc.perform(
                        get(BASE_JOB + "/" + jobB.job().getId() + "/applications")
                                .with(companyB.auth())
                                .param("page", "0")
                                .param("size", "10")
                                .param("from", LocalDate.now().minusYears(5).toString())
                                .param("to", LocalDate.now().plusDays(1).toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))   // <-- since your DTO has it
                .andExpect(jsonPath("$.content[*].jobId", containsInAnyOrder(
                        jobB.job().getId().intValue(), jobB.job().getId().intValue()
                )))
                .andExpect(jsonPath("$.content[*].applicationId", containsInAnyOrder(
                        app1.getId().intValue(), app2.getId().intValue()
                )))
                .andExpect(jsonPath("$.content[*].status", containsInAnyOrder("PENDING", "PENDING")));
    }

    @Test
    void companyB_canFilter_itsOwn_jobApplications_byStatus_returnsOnlyMatching() throws Exception {
        CompanyCtx companyB = companyCtx("B Inc Pos");
        JobCtx jobB = jobCtx(companyB, "Backend Dev B");

        User cand1 = candidate("cand1");
        User cand2 = candidate("cand2");

        JobApplication appPending = newPendingApplication(companyB.company(), jobB.job(), cand1);

        JobApplication appRejected = newPendingApplication(companyB.company(), jobB.job(), cand2);
        appRejected.setStatus(JobApplicationStatus.REJECTED);


        mockMvc.perform(
                        get(BASE_JOB + "/" + jobB.job().getId() + "/applications")
                                .with(companyB.auth())
                                .param("page", "0")
                                .param("size", "10")
                                .param("from", LocalDate.now().minusYears(5).toString())
                                .param("to", LocalDate.now().plusDays(1).toString())
                                .param("status", "REJECTED") // <-- if your param is named differently, change this
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].applicationId").value(appRejected.getId().intValue()))
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.content[0].jobId").value(jobB.job.getId().intValue()));
    }

    @Test
    void companyB_dateRangeFilter_returnsOnlyApplicationsInRange() throws Exception {
        CompanyCtx companyB = companyCtx("B Inc Date");
        JobCtx jobB = jobCtx(companyB, "Backend Dev B");

        User cand1 = candidate("cand1");
        User cand2 = candidate("cand2");

        newPendingApplication(companyB.company(), jobB.job(), cand1);
        newPendingApplication(companyB.company(), jobB.job(), cand2);

        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(2);


        mockMvc.perform(
                        get(BASE_JOB + "/" + jobB.job.getId() + "/applications")
                                .with(companyB.auth())
                                .param("page", "0")
                                .param("size", "10")
                                .param("from", from.toString())
                                .param("to", to.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void companyB_patchInvalidStatus_returns400_andDoesNotChangeStatus() throws Exception {
        Role companyRole = ensureRole(AppRole.ROLE_COMPANY);
        Role userRole = ensureRole(AppRole.ROLE_USER);

        // Company B
        User ownerB = newUser("ownerB_badstatus", "ownerB_badstatus@test.com", companyRole);
        Company companyB = newCompany("B Inc BadStatus", ownerB);

        Job jobB = newJob(companyB);
        User cand = newUser("cand_badstatus", "cand_badstatus@test.com", userRole);

        JobApplication app = newPendingApplication(companyB, jobB, cand);
        JobApplicationStatus original = app.getStatus();

        mockMvc.perform(
                        patch(BASE_APP + "/" + app.getId() + "/status")
                                .with(companyPrincipal(ownerB))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"NOT_A_REAL_STATUS\"}")
                )


                .andExpect(status().isBadRequest());

        JobApplication reloaded = jobApplicationRepository.findById(app.getId()).orElseThrow();
        Assertions.assertEquals(original, reloaded.getStatus());
    }



}
