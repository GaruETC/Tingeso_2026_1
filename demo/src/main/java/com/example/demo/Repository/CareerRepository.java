package com.example.demo.Repository;

import com.example.demo.Entity.CareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<CareerEntity, Long> {
}
