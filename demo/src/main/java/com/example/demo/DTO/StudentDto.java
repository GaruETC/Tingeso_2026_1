package com.example.demo.DTO;

import com.example.demo.Entity.Enum.StudentStatus;

public record StudentDto(
        String run,
        String email,
        String fullName,
        Long careerId,
        Long studyPlanId
) {
}
