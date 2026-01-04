package com.secure.jobs.dto.degreefield;

import lombok.Builder;
import lombok.Data;


@Builder
public record DegreeFieldOptionResponse(Long id, String name) {
}
