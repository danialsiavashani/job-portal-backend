package com.secure.jobs.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateCompanyEnabledRequest {
    @NotNull
    private Boolean enabled;
}
