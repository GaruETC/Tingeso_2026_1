package com.example.demo.Repository;

import com.example.demo.Entity.SectionEntity;
import com.example.demo.Entity.TeacherEntity;
import org.apache.el.parser.BooleanNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    List<SectionEntity> findByAcademicTermId(Long academicTermId);

    List<SectionEntity> findByTeacherId(Long teacherId);

    Optional<SectionEntity> findBySubjectId(Long subjectId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByTeacherIdAndAcademicTermId(Long teacherId, Long academicTermId);

    boolean existsBySubjectIdAndAcademicTermId(Long subjectId, Long termId);

    List<SectionEntity> findByTeacherIdAndAcademicTermId(Long teacherId, Long academicTermId);

    Optional<SectionEntity> findBySubjectIdAndAcademicTermId(Long subjectId, Long academicTermId);
}
