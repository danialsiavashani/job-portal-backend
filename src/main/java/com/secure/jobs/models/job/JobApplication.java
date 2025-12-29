package com.secure.jobs.models.job;

import com.secure.jobs.models.auth.User;
import com.secure.jobs.models.company.Company;
import com.secure.jobs.models.company.CompanyApplication;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_application_job_user",
                        columnNames = {"job_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_job_app_company_created_at", columnList = "company_id, created_at"),
                @Index(name = "idx_job_app_company_status_created_at", columnList = "company_id, status, created_at")
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // applicant user (REQUIRED because apply requires login)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // job (REQUIRED)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // optional but useful: server derived from job.getCompany()
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Verification document (Cloudinary)
    @Column(name = "document_public_id")
    private String documentPublicId;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobApplicationStatus status;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // Hibernate-safe equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobApplication other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
