package com.secure.jobs.dto.company;

import com.secure.jobs.dto.job.JobApplicationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanyJobApplicationPageResponse {

    private List<CompanyJobApplicationRowResponse> content;

    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
