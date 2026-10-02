package com.example.demo.Entity;

import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "academicHistorial")
public class AcademicHistorialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long subjectId;
    private Long academicTermId;

    private int failedNumber;
    private float finalGrade;
    private AcademicHistorialStatus status;
}
