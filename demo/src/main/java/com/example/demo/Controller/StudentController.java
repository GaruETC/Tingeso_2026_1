package com.example.demo.Controller;

import com.example.demo.DTO.StudentDto;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Service.StudentService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // Get all students (status or not)
    @GetMapping
    public ResponseEntity<List<StudentEntity>> getAllStudents(
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
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable Long id){
        StudentEntity student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
    // Create a new student
    @PostMapping
    public ResponseEntity<StudentEntity> createStudent(@RequestBody StudentDto dto){
        StudentEntity student = studentService.registerStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }
    // Update student information
    @PutMapping("/{id}")
    public ResponseEntity<StudentEntity> updateStudent(
            @PathVariable Long id, @RequestBody StudentDto dto){
        StudentEntity update = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(update);
    }
    // Update student status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStudentStatus(
            @PathVariable Long id, @RequestParam StudentStatus status){
        studentService.updateStudentStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id){
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
