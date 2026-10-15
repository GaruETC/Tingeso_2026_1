package com.example.demo.DTO;

import com.example.demo.Entity.Enum.TeacherStatus;

public record TeacherDto(
        String run,
        String email,
        String fullName,
        String title,
        String degree
) {
}
