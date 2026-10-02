package com.example.demo.Repository;

import com.example.demo.Entity.StudyPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlanEntity, Long> {
}
