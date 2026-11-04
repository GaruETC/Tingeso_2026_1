package com.example.demo.Repository;

import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByRun(String run);

    Optional<StudentEntity> findByEmail(String email);

    List<StudentEntity> findByCareerId(Long careerId);

    List<StudentEntity> findByStudyPlanId(Long studyPlanId);

    List<StudentEntity> findByStatus(StudentStatus status);

    boolean existsByStudyPlanId(Long studyPlanId);

    boolean existsByCareerId(Long careerId);

    boolean existsByRun(String run);

    boolean existsByEmail(String email);

    boolean existsByRunAndIdNot(String run, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Modifying
    @Query("UPDATE StudentEntity s SET s.matriculation = false")
    void resetAllMatriculations();
}
