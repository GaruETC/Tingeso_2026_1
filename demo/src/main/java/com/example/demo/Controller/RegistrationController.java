package com.example.demo.Controller;

import com.example.demo.DTO.RegistrationDto;
import com.example.demo.DTO.StudentDto;
import com.example.demo.DTO.SubmitGradesDto;
import com.example.demo.Entity.RegistrationEntity;
import com.example.demo.Service.RegistrationService;
import jakarta.servlet.Registration;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    public ResponseEntity<List<RegistrationEntity>> getRegistrations(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long termId){
        List<RegistrationEntity> registrations;
        if (studentId != null && termId != null){
            registrations = registrationService.getRegistrationsByStudentIdAndTermId(studentId, termId);
        } else if (sectionId != null){
            registrations = registrationService.getRegistrationsBySectionId(sectionId);
        } else{
            registrations = registrationService.getRegistrations();
        }
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationEntity> getRegistrationById(@PathVariable Long id){
        return ResponseEntity.ok(registrationService.getRegistrationById(id));
    }

    @PostMapping
    public ResponseEntity<RegistrationEntity> createRegistration(@RequestBody RegistrationDto dto){
        RegistrationEntity created = registrationService.createRegistration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id){
        registrationService.cancelRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> submitGrades(@PathVariable Long sectionId, @RequestBody SubmitGradesDto dto){
        registrationService.submitGrades(sectionId, dto);
        return ResponseEntity.noContent().build();
    }
}
