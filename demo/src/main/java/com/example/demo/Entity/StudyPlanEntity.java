package com.example.demo.Entity;

import com.example.demo.Entity.Enum.StudyPlanValidity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;

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
    private StudyPlanValidity validity = StudyPlanValidity.VIGENTE;
}
