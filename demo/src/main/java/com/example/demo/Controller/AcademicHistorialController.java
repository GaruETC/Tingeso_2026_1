package com.example.demo.Controller;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Service.AcademicHistorialService;
import com.example.demo.Service.AcademicTermService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-historial")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AcademicHistorialController {

    private final AcademicHistorialService academicHistorialService;

    @GetMapping("/student/{studentId}/passed-records")
    public ResponseEntity<List<AcademicHistorialEntity>> getPassedRecordsByStudent(@PathVariable Long studentId){
        List<AcademicHistorialEntity> passedSubjects = academicHistorialService.getPassedSubjectsByStudent(studentId);
        return ResponseEntity.ok(passedSubjects);
    }
}
