package com.example.demo.Repository;

import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerRepository extends JpaRepository<CareerEntity, Long> {

    Optional<CareerEntity> findByCode(String code);

    Optional<CareerEntity> findByName(String name);

    List<CareerEntity> findByStatus(CareerStatus status);
}
