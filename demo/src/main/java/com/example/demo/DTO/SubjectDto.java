package com.example.demo.DTO;

import java.util.List;

public record SubjectDto(
        String code,
        String name,
        String TEL,
        int SCT,
        int semester,
        List<Long> prerequisiteIds,
        Long studyPlanId
) {
}
