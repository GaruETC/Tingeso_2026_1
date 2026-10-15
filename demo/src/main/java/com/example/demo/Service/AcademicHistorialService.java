package com.example.demo.Service;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Repository.AcademicHistorialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcademicHistorialService {

    private final AcademicHistorialRepository academicHistorialRepository;

    public AcademicHistorialService(
            AcademicHistorialRepository academicHistorialRepository) {
        this.academicHistorialRepository = academicHistorialRepository;
    }

    public List<AcademicHistorialEntity> getPassedSubjectsByStudent(Long studentId) {
        return academicHistorialRepository.findByStudentIdAndStatus(studentId, AcademicHistorialStatus.PASSED);
    }
}
