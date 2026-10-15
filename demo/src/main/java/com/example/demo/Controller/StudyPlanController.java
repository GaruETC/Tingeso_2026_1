package com.example.demo.Controller;

import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudyPlanController {

    private StudyPlanService studyPlanService;

    @GetMapping
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
    public ResponseEntity<StudyPlanEntity> getStudyPlanById(@PathVariable Long id){
        return ResponseEntity.ok(studyPlanService.getStudyPlanById(id));
    }

    @PostMapping
    public ResponseEntity<StudyPlanEntity> createStudyPlan(@RequestBody Long careerId){
        StudyPlanEntity created = studyPlanService.createStudyPlan(careerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable Long id){
        studyPlanService.deleteStudyPlan(id);
        return ResponseEntity.noContent().build();
    }
}
