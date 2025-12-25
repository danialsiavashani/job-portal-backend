package com.secure.jobs.dto.job;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JobApplicationPageResponse {

    private List<JobApplicationResponse> content;

    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
