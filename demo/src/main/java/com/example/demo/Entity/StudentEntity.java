package com.example.demo.Entity;

import com.example.demo.Entity.Enum.StudentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "students")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String run;
    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;
    private boolean matriculation;

    @Column(nullable = false)
    private Long careerId;
    @Column(nullable = false)
    private Long studyPlanId;
    private List<String> subject;// cuidado con esto

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.REGULAR;
}
