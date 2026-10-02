package com.example.demo.Entity;

import com.example.demo.Entity.Enum.CareerStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "career")
public class CareerEntity {

    // duracion en semestres
    public static final int DURATION = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String code;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private CareerStatus status;
}
