package com.example.demo.Service;

import com.example.demo.Repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;

    public StudyPlanService(StudyPlanRepository studyPlanRepository) {
        this.studyPlanRepository = studyPlanRepository;
    }
}
