package com.example.demo.Service;

import com.example.demo.Repository.CareerRepository;
import org.springframework.stereotype.Service;

@Service
public class CareerService {

    private final CareerRepository careerRepository;

    public CareerService(CareerRepository careerRepository) {
        this.careerRepository = careerRepository;
    }
}
