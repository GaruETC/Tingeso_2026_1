package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "subjects")
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;
    private String name;
    private String TEL;
    private int SCT;
    private int semester;

    @ElementCollection
    @CollectionTable(
            name = "subject_prerequisites",
            joinColumns = @JoinColumn(name = "subject_id")
    )
    @Column(name = "prerequisite_id")
    private List<Long> prerequisiteIds = new ArrayList<>(); // cuidado con esto

    private Long studyPlanId;
}
