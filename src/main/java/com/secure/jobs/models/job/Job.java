package com.secure.jobs.models.job;

import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.user.profile.DegreeField;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_jobs_company_created_at", columnList = "company_id, created_at"),
                @Index(name = "idx_jobs_company_status_created_at", columnList = "company_id, status, created_at")
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String tagline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_degree_fields",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "degree_field_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_job_degree_field",
                    columnNames = {"job_id", "degree_field_id"}
            )
    )
    private Set<DegreeField> degreeFields = new HashSet<>();

    @Column(nullable = false)
    private int numberOfApplicants = 0;

    private String level;

    private String location;

    @Column(precision = 12, scale = 2)
    private BigDecimal payMin;

    @Column(precision = 12, scale = 2)
    private BigDecimal payMax;

    @Enumerated(EnumType.STRING)
    private PayPeriod payPeriod;

    @Enumerated(EnumType.STRING)
    private PayType payType;

    /* =======================
       Arrays → ElementCollection
    ======================== */
    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "job_benefits",
            joinColumns = @JoinColumn(name = "job_id")
    )
    @Column(name = "benefit")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> benefits = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "job_minimum_requirements",
            joinColumns = @JoinColumn(name = "job_id")
    )
    @Column(name = "requirement")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> minimumRequirements = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private Set<SavedJob> savedJobs = new HashSet<>();

    public void incrementApplicants() {
        this.numberOfApplicants++;
    }


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
