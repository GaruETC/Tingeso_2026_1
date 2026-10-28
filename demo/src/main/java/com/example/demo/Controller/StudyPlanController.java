package com.example.demo.Controller;

import com.example.demo.DTO.StudyPlanDto;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Service.StudyPlanService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class StudyPlanController {

    private StudyPlanService studyPlanService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudyPlanEntity>> getStudyPlans(
            @RequestParam(required = false) Long careerId) {
        List<StudyPlanEntity> studyPlans;
        if (careerId != null){
            studyPlans = studyPlanService.getStudyPlansByCareerId(careerId);
        } else{
            studyPlans = studyPlanService.getStudyPlans();
        }
        return ResponseEntity.ok(studyPlans);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<StudyPlanEntity> getStudyPlanById(@PathVariable Long id){
        return ResponseEntity.ok(studyPlanService.getStudyPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudyPlanEntity> createStudyPlan(@RequestBody StudyPlanDto Dto){
        StudyPlanEntity created = studyPlanService.createStudyPlan(Dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable Long id){
        studyPlanService.deleteStudyPlan(id);
        return ResponseEntity.noContent().build();
    }
}
