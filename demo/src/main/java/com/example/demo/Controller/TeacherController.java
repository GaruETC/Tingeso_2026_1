package com.example.demo.Controller;

import com.example.demo.DTO.TeacherDto;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Service.TeacherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    //get all teachers (with status or not)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherEntity>> getTeachers(
            @RequestParam(required = false)TeacherStatus status){
        List<TeacherEntity> teachers;
        if (status != null){
            teachers = teacherService.getTeachersByStatus(status);
        }else{
            teachers = teacherService.getTeachers();
        }
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<TeacherEntity> getTeacherById(@PathVariable Long id){
        TeacherEntity teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(teacher);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherEntity> createTeacher(@RequestBody TeacherDto dto){
        TeacherEntity created = teacherService.registerTeacher(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherEntity> updateTeacher(
            @PathVariable Long id, @RequestBody TeacherDto dto){
        TeacherEntity updated = teacherService.updateTeacher(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTeacherStatus(
            @PathVariable Long id, @RequestParam TeacherStatus status){
        teacherService.updateTeacherStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id){
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherEntity> getMyProfile(Authentication authentication) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;

        String email = (String) jwtAuthToken.getTokenAttributes().get("email");
        TeacherEntity teacher = teacherService.getTeacherByEmail(email);

        return ResponseEntity.ok(teacher);
    }
}
