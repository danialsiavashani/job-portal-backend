package com.secure.jobs.models.user.profile;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "degree_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
