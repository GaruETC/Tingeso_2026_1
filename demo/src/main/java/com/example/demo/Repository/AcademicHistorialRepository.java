package com.example.demo.Repository;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicHistorialRepository extends JpaRepository<AcademicHistorialEntity, Long> {

    List<AcademicHistorialEntity> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsBySubjectId(Long subjectId);

    List<AcademicHistorialEntity> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    boolean existsByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, AcademicHistorialStatus status);

    int countByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, AcademicHistorialStatus status);

    int countByStudentIdAndStatus(Long studentId, AcademicHistorialStatus status);
}
