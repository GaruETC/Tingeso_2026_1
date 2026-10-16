package com.example.demo.Controller;

import com.example.demo.DTO.SectionDto;
import com.example.demo.Entity.SectionEntity;
import com.example.demo.Service.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private SectionService sectionService;

    @GetMapping
    public ResponseEntity<List<SectionEntity>> getSections(
            @RequestParam(required = false) Long teacherId, @RequestParam(required = false) Long academicTermId){
        List<SectionEntity> sections;
        if(teacherId != null && academicTermId != null){
            sections = sectionService.getSectionsByTeacherIdAndAcademicTermId(teacherId, academicTermId);
        } else if (teacherId != null){
            sections = sectionService.getSectionsByTeacherId(teacherId);
        } else if (academicTermId != null){
            sections = sectionService.getSectionsByAcademicTermId(academicTermId);
        } else{
            sections = sectionService.getSections();
        }
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionEntity> getSectionById(@PathVariable Long id){
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PostMapping
    public ResponseEntity<SectionEntity> createSection(@RequestBody SectionDto dto){
        SectionEntity created = sectionService.createSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id){
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
