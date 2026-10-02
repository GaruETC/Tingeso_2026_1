package com.example.demo.Repository;

import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudyPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlanEntity, Long> {

    List<StudyPlanEntity> findByCareerId(Long careerId);

    Optional<StudyPlanEntity> findByCareerIdAndStatus(
            Long careerId,
            StudyPlanStatus status
    );
}
