package com.example.demo.Controller;

import com.example.demo.DTO.AcademicTermDto;
import com.example.demo.Entity.AcademicTermEntity;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Service.AcademicTermService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-terms")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AcademicTermController {

    private final AcademicTermService academicTermService;

    @GetMapping
    public ResponseEntity<List<AcademicTermEntity>> getAcademicTerms(
            @RequestParam(required = false) AcademicTermStatus status){
        List<AcademicTermEntity> terms;
        if (status != null){
            terms = academicTermService.getTermsByStatus(status);
        } else{
            terms = academicTermService.getAcademicTerms();
        }
        return ResponseEntity.ok(terms);
    }

    @GetMapping("/current")
    public ResponseEntity<AcademicTermEntity> getCurrentAcademicTerm(){
        return academicTermService.getActiveTerm()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicTermEntity> getAcademicTermById(@PathVariable Long id){
        return ResponseEntity.ok(academicTermService.getAcademicTermById(id));
    }

    @PostMapping
    public ResponseEntity<AcademicTermEntity> createAcademicTerm(
            @RequestBody AcademicTermDto dto){
        AcademicTermEntity created = academicTermService.createAcademicTerm(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeAcademicTerm(@PathVariable Long id){
        academicTermService.closeAcademicTerm(id);
        return ResponseEntity.noContent().build();
    }
}
