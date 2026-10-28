package com.example.demo.DTO;

public record StudentDto(
        String run,
        String email,
        String fullName,
        Long careerId,
        Long studyPlanId
) {
}
