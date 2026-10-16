package com.example.demo.Repository;

import com.example.demo.Entity.AcademicTermEntity;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface AcademicTermRepository extends JpaRepository<AcademicTermEntity, Long> {

    Optional<AcademicTermEntity> findByPeriod(String period);

    //for inactives
    List<AcademicTermEntity> findByStatus(AcademicTermStatus status);

    //for the active
    Optional<AcademicTermEntity> findFirstByStatus(AcademicTermStatus status);

    boolean existsByPeriod(String period);

    boolean existsByStatus(AcademicTermStatus status);
}
