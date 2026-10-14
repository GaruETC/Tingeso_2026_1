package com.example.demo.DTO;

import java.util.List;

public record SubmitGradesDto(
        Long teacherId,

        List<StudentGradeDto> grades
) {
    public record StudentGradeDto(
            Long registrationId,

            Float grade
    ) {}
}
