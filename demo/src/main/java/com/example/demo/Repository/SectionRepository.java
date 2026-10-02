package com.example.demo.Repository;

import com.example.demo.Entity.SectionEntity;
import com.example.demo.Entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    List<SectionEntity> findByAcademicTermId(Long academicTermId);

    List<SectionEntity> findByTeacherId(Long teacherId);

    SectionEntity findBySubjectId(Long subjectId);

}
