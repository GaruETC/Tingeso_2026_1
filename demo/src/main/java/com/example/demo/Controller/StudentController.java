package com.example.demo.Controller;

import com.example.demo.DTO.StudentDto;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Service.StudentService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // Get all students (status or not)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentEntity>> getStudents(
            @RequestParam(required = false)StudentStatus status){
        List<StudentEntity> students;
        if (status != null){
            students = studentService.getStudentsByStatus(status);
        }else{
            students = studentService.getStudents();
        }
        return ResponseEntity.ok(students);
    }
    // Get a student by his ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable Long id){
        StudentEntity student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
    // Create a new student
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentEntity> createStudent(@RequestBody StudentDto dto){
        StudentEntity student = studentService.registerStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }
    // Update student information
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentEntity> updateStudent(
            @PathVariable Long id, @RequestBody StudentDto dto){
        StudentEntity update = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(update);
    }
    // Update student status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStudentStatus(
            @PathVariable Long id, @RequestParam StudentStatus status){
        studentService.updateStudentStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id){
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentEntity> getMyProfile(Authentication authentication) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;

        String email = (String) jwtAuthToken.getTokenAttributes().get("email");
        StudentEntity student = studentService.getStudentByEmail(email);

        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}/matricular")
    public ResponseEntity<StudentEntity> simularMatricula(@PathVariable Long id) {
        StudentEntity estudiante = studentService.matricularEstudiante(id);
        return ResponseEntity.ok(estudiante);
    }
}
