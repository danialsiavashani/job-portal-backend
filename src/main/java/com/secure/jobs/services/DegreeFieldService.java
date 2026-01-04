package com.secure.jobs.services;


import com.secure.jobs.dto.admin.DegreeFieldCreateRequest;
import com.secure.jobs.dto.admin.DegreeFieldResponse;
import com.secure.jobs.dto.admin.DegreeFieldUpdateRequest;
import com.secure.jobs.dto.degreefield.DegreeFieldOptionResponse;
import com.secure.jobs.models.user.profile.DegreeField;

import java.util.List;

public interface DegreeFieldService {
    DegreeFieldResponse create(DegreeFieldCreateRequest request);
    DegreeFieldResponse update(Long id, DegreeFieldUpdateRequest request);
    List<DegreeFieldResponse> findAll();
    List<DegreeFieldOptionResponse> getActiveDegreeFields();
}
