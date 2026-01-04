package com.secure.jobs.services.iml;


import com.secure.jobs.dto.admin.DegreeFieldCreateRequest;
import com.secure.jobs.dto.admin.DegreeFieldResponse;
import com.secure.jobs.dto.admin.DegreeFieldUpdateRequest;
import com.secure.jobs.dto.degreefield.DegreeFieldOptionResponse;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.mappers.DegreeFieldMapper;
import com.secure.jobs.models.user.profile.DegreeField;
import com.secure.jobs.repositories.DegreeFieldRepository;
import com.secure.jobs.services.DegreeFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DegreeFieldServiceImpl implements DegreeFieldService {

    private final DegreeFieldRepository degreeFieldRepository;

    @Override
    public DegreeFieldResponse create(DegreeFieldCreateRequest request) {
        String name = normalize(request.getName());

        if (degreeFieldRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException("Degree field already exists: " + name, HttpStatus.CONFLICT);
        }

        DegreeField df = DegreeField.builder()
                .name(name)
                .active(true)
                .build();

        degreeFieldRepository.save(df);
        return DegreeFieldMapper.toResponse(df);
    }

    @Override
    public DegreeFieldResponse update(Long id, DegreeFieldUpdateRequest request) {
        if (request.getName() == null && request.getActive() == null) {
            throw new BadRequestException("No fields provided to update.");
        }
        DegreeField df = degreeFieldRepository.findById(id)
                .orElseThrow(() -> new ApiException("Degree field not found: " ,HttpStatus.NOT_FOUND));

        if (request.getName() != null) {
            String name = normalize(request.getName());
            if (!name.equalsIgnoreCase(df.getName()) && degreeFieldRepository.existsByNameIgnoreCase(name)) {
                throw new ApiException("Degree field already exists: " + name, HttpStatus.CONFLICT);
            }
            df.setName(name);
        }

        if (request.getActive() != null) {
            df.setActive(request.getActive());
        }

        return DegreeFieldMapper.toResponse(df);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeFieldResponse> findAll() {
        List<DegreeField> degreeField = degreeFieldRepository.findAll();
        return DegreeFieldMapper.toResponseList(degreeField);
    }
    

    @Override
    public List<DegreeFieldOptionResponse> getActiveDegreeFields() {
        List<DegreeField> df = degreeFieldRepository.findByActiveTrueOrderByNameAsc();
        return DegreeFieldMapper.toOptionResponseList(df);
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }
}
