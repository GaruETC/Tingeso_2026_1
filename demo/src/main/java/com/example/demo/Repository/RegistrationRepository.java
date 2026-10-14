package com.example.demo.Repository;

import com.example.demo.Entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    List<RegistrationEntity> findByStudentId(Long studentId);

    List<RegistrationEntity> findBySectionId(Long sectionId);

    List<RegistrationEntity> findByAcademicTermId(Long academicTermId);

    List<RegistrationEntity> findByStudentIdAndAcademicTermId(Long studentId, Long academicTermId);

    boolean existsByStudentId(Long studentId);

    boolean existsBySectionId(Long sectionId);

    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    Long countBySectionId(Long sectionId);
}
