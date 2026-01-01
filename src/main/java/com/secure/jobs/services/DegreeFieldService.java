package com.secure.jobs.services;


import com.secure.jobs.dto.admin.DegreeFieldCreateRequest;
import com.secure.jobs.dto.admin.DegreeFieldUpdateRequest;
import com.secure.jobs.models.user.profile.DegreeField;

import java.util.List;

public interface DegreeFieldService {
    DegreeField create(DegreeFieldCreateRequest request);
    DegreeField update(Long id, DegreeFieldUpdateRequest request);
    List<DegreeField> findAll();
    List<DegreeField> findActive();
}
