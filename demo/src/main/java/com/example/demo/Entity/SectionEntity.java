package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "sections")
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teacherId;
    @Column(nullable = false)
    private Long subjectId;
    @Column(nullable = false)
    private Long academicTermId;
    @Column(nullable = false)
    private List<String> schedule;
    @Column(nullable = false)
    private int maxCapacity;
}
