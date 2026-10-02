package com.example.demo.Repository;

import com.example.demo.Entity.AcademicTermEntity;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicTermRepository extends JpaRepository<AcademicTermEntity, Long> {

    Optional<AcademicTermEntity> findByPeriod(String period);

    List<AcademicTermEntity> findByStatus(AcademicTermStatus status);

}
