package com.example.demo.Repository;

import com.example.demo.Entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {

    SubjectEntity findByCode(String code);

    SubjectEntity findByName(String name);

    List<SubjectEntity> findBySemester(int semester);

    List<SubjectEntity> findByStudyPlanId(Long studyPlanId);

    List<SubjectEntity> findByPrerequisiteIdsContaining(Long id);

}
