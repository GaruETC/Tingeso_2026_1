package com.example.demo.Repository;

import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<CareerEntity, Long> {

    CareerEntity findByCode(String code);

    CareerEntity findByName(String name);

    List<CareerEntity> findByStatus(CareerStatus status);
}
