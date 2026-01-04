package com.secure.jobs.controllers.admin;


import com.secure.jobs.dto.admin.DegreeFieldCreateRequest;
import com.secure.jobs.dto.admin.DegreeFieldResponse;
import com.secure.jobs.dto.admin.DegreeFieldUpdateRequest;
import com.secure.jobs.mappers.DegreeFieldMapper;
import com.secure.jobs.services.DegreeFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/degree-fields")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class DegreeFieldAdminController {

    private final DegreeFieldService degreeFieldService;

    @PostMapping
    public DegreeFieldResponse create(@Valid @RequestBody DegreeFieldCreateRequest request) {
        return degreeFieldService.create(request);
    }

    @PatchMapping("/{id}")
    public DegreeFieldResponse update(@PathVariable Long id,@Valid  @RequestBody DegreeFieldUpdateRequest request) {
        return degreeFieldService.update(id, request);
    }

    @GetMapping
    public List<DegreeFieldResponse> listAll() {
        return degreeFieldService.findAll();
    }
}
