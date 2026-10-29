package com.example.demo.Repository;

import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    Optional<TeacherEntity> findByRun(String run);

    Optional<TeacherEntity> findByFullName(String fullName);

    List<TeacherEntity> findByStatus(TeacherStatus status);

    Optional<TeacherEntity> findByEmail(String email);

    boolean existsByRun(String run);

    boolean existsByEmail(String email);

    boolean existsByRunAndIdNot(String run, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
