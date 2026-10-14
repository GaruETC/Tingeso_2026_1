package com.example.demo.DTO;

import com.example.demo.Entity.Enum.CareerStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record CareerDto(
        String code,
        String name,
        String description,
        CareerStatus status
) {
}
