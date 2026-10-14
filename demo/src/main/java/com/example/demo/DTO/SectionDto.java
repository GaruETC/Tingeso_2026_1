package com.example.demo.DTO;

import java.util.List;

public record SectionDto(
        Long teacherId,
        Long subjectId,
        Long academicTermId,
        List<String> schedule,
        int maxCapacity
) {
}
