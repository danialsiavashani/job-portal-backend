package com.secure.jobs.services.iml;


import com.secure.jobs.dto.admin.DegreeFieldCreateRequest;
import com.secure.jobs.dto.admin.DegreeFieldUpdateRequest;
import com.secure.jobs.exceptions.ApiException;
import com.secure.jobs.exceptions.BadRequestException;
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
    public DegreeField create(DegreeFieldCreateRequest request) {
        String name = normalize(request.getName());

        if (degreeFieldRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException("Degree field already exists: ", HttpStatus.BAD_REQUEST);
        }

        DegreeField df = DegreeField.builder()
                .name(name)
                .active(true)
                .build();

        return degreeFieldRepository.save(df);
    }

    @Override
    public DegreeField update(Long id, DegreeFieldUpdateRequest request) {
        DegreeField df = degreeFieldRepository.findById(id)
                .orElseThrow(() -> new ApiException("Degree field not found: " ,HttpStatus.BAD_REQUEST));

        if (request.getName() != null) {
            String name = normalize(request.getName());
            if (!name.equalsIgnoreCase(df.getName()) && degreeFieldRepository.existsByNameIgnoreCase(name)) {
                throw new BadRequestException("Degree field already exists: " + name);
            }
            df.setName(name);
        }

        if (request.getActive() != null) {
            df.setActive(request.getActive());
        }

        return degreeFieldRepository.save(df);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeField> findAll() {
        return degreeFieldRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeField> findActive() {
        return degreeFieldRepository.findByActiveTrueOrderByNameAsc();
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }
}
