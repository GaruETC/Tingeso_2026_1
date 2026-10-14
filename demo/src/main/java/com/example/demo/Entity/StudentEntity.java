package com.example.demo.Entity;

import com.example.demo.Entity.Enum.StudentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private boolean matriculation = false;

    @Column(nullable = false)
    private Long careerId;
    @Column(nullable = false)
    private Long studyPlanId;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.REGULAR;

}
