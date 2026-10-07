package com.example.demo.Repository;

import com.example.demo.Entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {

    Optional<SubjectEntity> findByCode(String code);

    Optional<SubjectEntity> findByName(String name);

    List<SubjectEntity> findBySemester(int semester);

    List<SubjectEntity> findByStudyPlanId(Long studyPlanId);

    List<SubjectEntity> findByPrerequisiteIdsContaining(Long prerequisiteId);

}
