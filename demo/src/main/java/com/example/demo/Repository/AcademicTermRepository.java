package com.example.demo.Repository;

import com.example.demo.Entity.AcademicTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicTermRepository extends JpaRepository<AcademicTermEntity, Long> {
}
