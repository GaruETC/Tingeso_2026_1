package com.example.demo.Controller;

import com.example.demo.DTO.SubjectDto;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.Subject;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<SubjectEntity>> getSubjects(
            @RequestParam(required = false) Long studyPlanId) {
        List<SubjectEntity> subjects;
        if (studyPlanId != null){
            subjects = subjectService.getSubjectsByStudyPlanId(studyPlanId);
        } else{
            subjects = subjectService.getSubjects();
        }
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SubjectEntity> getSubjectById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectEntity> createSubject(@RequestBody SubjectDto subjectDto) {
        SubjectEntity created = subjectService.registerSubject(subjectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectEntity> updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectDto subjectDto) {
        SubjectEntity updated = subjectService.updateSubject(id, subjectDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/prerequisites")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubjectEntity>> getPrerequisites(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getPrerequisites(id));
    }
}
