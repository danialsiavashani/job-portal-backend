package com.secure.jobs.dto.admin;

import lombok.*;

@Data
@Builder
public class DegreeFieldResponse {
    private Long id;
    private String name;
    private boolean active;
}

