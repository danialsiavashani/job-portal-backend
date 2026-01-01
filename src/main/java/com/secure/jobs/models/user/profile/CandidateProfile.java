package com.secure.jobs.models.user.profile;

import com.secure.jobs.models.user.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Shared primary key:
     * candidate_profiles.user_id is BOTH PK and FK to users.user_id
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    // Resume (Cloudinary)
    @Column(name = "resume_public_id")
    private String resumePublicId;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    // Education
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 30)
    private EducationLevel educationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_field_id")
    private DegreeField degreeField;

    // Experience / Contact
    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String location;
}
