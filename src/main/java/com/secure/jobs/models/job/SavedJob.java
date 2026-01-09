package com.secure.jobs.models.job;

import com.secure.jobs.models.user.auth.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "saved_jobs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_job_user_job",
                columnNames = {"user_id", "job_id"}
        ),
        indexes = {
                @Index(name = "idx_saved_jobs_user_created_at", columnList = "user_id, created_at"),
                @Index(name = "idx_saved_jobs_job", columnList = "job_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> users.user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK -> jobs.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Hibernate-safe equality
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedJob other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
