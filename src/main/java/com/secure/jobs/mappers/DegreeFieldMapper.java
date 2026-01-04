package com.secure.jobs.mappers;

import com.secure.jobs.dto.admin.DegreeFieldResponse;
import com.secure.jobs.dto.degreefield.DegreeFieldOptionResponse;
import com.secure.jobs.models.user.profile.DegreeField;

import java.util.List;

public class DegreeFieldMapper {

    public static DegreeFieldResponse toResponse(DegreeField df) {
        return DegreeFieldResponse.builder()
                .id(df.getId())
                .name(df.getName())
                .active(df.isActive())
                .build();
    }

    public static List<DegreeFieldResponse> toResponseList(List<DegreeField> list) {
        return list.stream().map(DegreeFieldMapper::toResponse).toList();
    }


    public static DegreeFieldOptionResponse toOptionResponse(DegreeField df) {
        return DegreeFieldOptionResponse.builder()
                .id(df.getId())
                .name(df.getName())
                .build();
    }


    public static List<DegreeFieldOptionResponse> toOptionResponseList(List<DegreeField> list) {
        return list.stream().map(DegreeFieldMapper::toOptionResponse).toList();
    }


}
