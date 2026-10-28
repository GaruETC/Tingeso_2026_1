package com.example.demo.Entity;

import com.example.demo.Entity.Enum.TeacherStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teachers")
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String run;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String degree;

    @Enumerated(EnumType.STRING)
    private TeacherStatus status = TeacherStatus.ACTIVO;
}
