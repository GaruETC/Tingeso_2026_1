package com.example.demo.Repository;

import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    TeacherEntity finByRun(String run);

    TeacherEntity findByFullName(String fullName);

    List<TeacherEntity> findByStatus(TeacherStatus status);
}
