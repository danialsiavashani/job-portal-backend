package com.secure.jobs.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCompanyApplicationPageResponse {

    private List<AdminCompanyApplicationResponse> content;

    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
