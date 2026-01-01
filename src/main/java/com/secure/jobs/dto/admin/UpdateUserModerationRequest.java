package com.secure.jobs.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserModerationRequest {
    private Boolean enabled;
    private Boolean accountNonLocked;
}
