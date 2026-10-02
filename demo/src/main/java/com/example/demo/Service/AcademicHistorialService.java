package com.example.demo.Service;

import com.example.demo.Repository.AcademicHistorialRepository;
import org.springframework.stereotype.Service;

@Service
public class AcademicHistorialService {

    private final AcademicHistorialRepository academicHistorialRepository;

    public AcademicHistorialService(
            AcademicHistorialRepository academicHistorialRepository) {
        this.academicHistorialRepository = academicHistorialRepository;
    }
}
