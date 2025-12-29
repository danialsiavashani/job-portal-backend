package com.secure.jobs.models.company;

import com.secure.jobs.models.auth.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_status_created_at", columnList = "status, created_at"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user applying to become a company.
     * A user can have only ONE active company application.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    // Verification document (Cloudinary)
    @Column(name = "document_public_id")
    private String documentPublicId;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyApplicationStatus status;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Hibernate-safe equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyApplication other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
