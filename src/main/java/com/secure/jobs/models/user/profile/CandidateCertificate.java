package com.secure.jobs.models.user.profile;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_certificates")
public class CandidateCertificate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_user_id")
    private CandidateProfile profile;

    @Column(nullable = false)
    private String name;

    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Column(length = 500)
    private String credentialUrl;
}
