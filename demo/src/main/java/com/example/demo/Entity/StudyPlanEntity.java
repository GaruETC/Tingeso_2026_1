package com.example.demo.Entity;

import com.example.demo.Entity.Enum.StudyPlanStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "studyPlans")
public class StudyPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long careerId;

    @Enumerated(EnumType.STRING)
    private StudyPlanStatus status = StudyPlanStatus.VIGENTE;
}
