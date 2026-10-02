package com.example.demo.Repository;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicHistorialRepository extends JpaRepository<AcademicHistorialEntity, Long> {

    List<AcademicHistorialEntity> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}
