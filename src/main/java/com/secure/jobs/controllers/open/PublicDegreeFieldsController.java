package com.secure.jobs.controllers.open;

import com.secure.jobs.dto.degreefield.DegreeFieldOptionResponse;
import com.secure.jobs.mappers.DegreeFieldMapper;
import com.secure.jobs.services.DegreeFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/degree-fields")
@RequiredArgsConstructor
public class PublicDegreeFieldsController {

    private final DegreeFieldService degreeFieldService;

    @GetMapping("/active")
    public List<DegreeFieldOptionResponse> getActiveDegreeFields() {
        return degreeFieldService.getActiveDegreeFields();
    }


}
