package com.example.demo.DTO;

import java.util.List;

public record SubjectDto(
        String code,
        String name,
        int theoryHours,
        int exerciseHours,
        int laboratoryHours,
        int SCT,
        int semester,
        List<Long> prerequisiteIds,
        Long studyPlanId
) {
}
