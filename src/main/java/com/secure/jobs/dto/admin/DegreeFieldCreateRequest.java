package com.secure.jobs.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class DegreeFieldCreateRequest {
    @NotBlank
    private String name;
}

