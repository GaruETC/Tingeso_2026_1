package com.example.demo.Controller;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Service.AcademicHistorialService;
import com.example.demo.Service.AcademicTermService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-historial")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AcademicHistorialController {

    private final AcademicHistorialService academicHistorialService;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<AcademicHistorialEntity>> getAcademicTerms(@PathVariable Long studentId){
        List<AcademicHistorialEntity> subjects = academicHistorialService.getAcademicTerms(studentId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/student/{studentId}/passed-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<AcademicHistorialEntity>> getPassedRecordsByStudent(@PathVariable Long studentId){
        List<AcademicHistorialEntity> passedSubjects = academicHistorialService.getPassedSubjectsByStudent(studentId);
        return ResponseEntity.ok(passedSubjects);
    }
}
