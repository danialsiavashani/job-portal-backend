package com.secure.jobs.models.user.profile;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_experiences")
public class CandidateExperience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_user_id")
    private CandidateProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceType type;

    @Column(nullable = false)
    private String title;

    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}
