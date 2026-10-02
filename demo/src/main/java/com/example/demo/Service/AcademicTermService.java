package com.example.demo.Service;

import com.example.demo.Repository.AcademicTermRepository;
import org.springframework.stereotype.Service;

@Service
public class AcademicTermService {

    private final AcademicTermRepository academicTermRepository;

    public AcademicTermService(AcademicTermRepository academicTermRepository) {
        this.academicTermRepository = academicTermRepository;
    }
}
