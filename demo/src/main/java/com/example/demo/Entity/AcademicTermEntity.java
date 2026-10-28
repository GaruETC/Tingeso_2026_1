package com.example.demo.Entity;

import com.example.demo.Entity.Enum.AcademicTermStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "academicTerms")
public class AcademicTermEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String period;
    private AcademicTermStatus status = AcademicTermStatus.ABIERTO;
}
