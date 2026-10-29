package com.example.demo.Service;

import com.example.demo.DTO.StudentDto;
import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AcademicHistorialRepository academicHistorialRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private StudentService studentService;

    private StudentEntity student;
    private StudentDto studentDto;
    private CareerEntity career;
    private StudyPlanEntity studyPlan;

    @BeforeEach
    void setUp() {
        student = new StudentEntity();
        student.setId(1L);
        student.setRun("12345678-9");
        student.setEmail("maria.gomez@usach.cl");
        student.setFullName("Maria Gomez");
        student.setCareerId(10L);
        student.setStudyPlanId(20L);
        student.setStatus(StudentStatus.REGULAR);

        studentDto = new StudentDto(
                "12345678-9",
                "maria.gomez@usach.cl",
                "Maria Gomez",
                10L,
                20L
        );

        career = new CareerEntity();
        career.setId(10L);
        career.setStatus(CareerStatus.ACTIVO);

        studyPlan = new StudyPlanEntity();
        studyPlan.setId(20L);
        studyPlan.setCareerId(10L);
        studyPlan.setStatus(StudyPlanStatus.VIGENTE);
    }

    @Test
    void whenGetStudents_thenReturnAllStudents() {
        //Given
        when(studentRepository.findAll()).thenReturn(List.of(student));

        //When
        List<StudentEntity> result = studentService.getStudents();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFullName()).isEqualTo("Maria Gomez");
    }

    @Test
    void whenGetStudentsByStatus_thenReturnFilteredStudents() {
        //Given
        when(studentRepository.findByStatus(StudentStatus.REGULAR)).thenReturn(List.of(student));

        //When
        List<StudentEntity> result = studentService.getStudentsByStatus(StudentStatus.REGULAR);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(StudentStatus.REGULAR);
    }

    @Test
    void whenGetStudentById_thenReturnStudent() {
        //Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        //When
        StudentEntity result = studentService.getStudentById(1L);

        //Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetStudentByEmail_thenReturnStudent() {
        //Given
        when(studentRepository.findByEmail("maria.gomez@usach.cl")).thenReturn(Optional.of(student));

        //When
        StudentEntity result = studentService.getStudentByEmail("maria.gomez@usach.cl");

        //Then
        assertThat(result.getEmail()).isEqualTo("maria.gomez@usach.cl");
    }

    @Test
    void whenGetStudentByRun_thenReturnStudent() {
        //Given
        when(studentRepository.findByRun("12345678-9")).thenReturn(Optional.of(student));

        //When
        StudentEntity result = studentService.getStudentByRun("12345678-9");

        //Then
        assertThat(result.getRun()).isEqualTo("12345678-9");
    }

    @Test
    void whenUpdateStudent_thenReturnUpdatedStudent() {
        //Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByRunAndIdNot("12345678-9", 1L)).thenReturn(false);
        when(studentRepository.existsByEmailAndIdNot("maria.gomez@usach.cl", 1L)).thenReturn(false);
        when(studentRepository.save(any(StudentEntity.class))).thenReturn(student);

        //When
        StudentEntity result = studentService.updateStudent(1L, studentDto);

        //Then
        assertThat(result.getFullName()).isEqualTo("Maria Gomez");
    }

    @Test
    void whenHasRecordsFromHistorial_thenReturnTrue() {
        //Given
        when(registrationRepository.existsByStudentId(1L)).thenReturn(false);
        when(academicHistorialRepository.existsByStudentId(1L)).thenReturn(true);

        //When
        boolean result = studentService.hasRecords(1L);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void whenDeleteStudent_thenVerifyDeletion() {
        //Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentId(1L)).thenReturn(false);
        when(academicHistorialRepository.existsByStudentId(1L)).thenReturn(false);

        //When
        studentService.deleteStudent(1L);

        //Then
        verify(studentRepository).delete(student);
    }

    @Test
    void whenDeleteStudentWithRecords_thenThrowException() {
        //Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(registrationRepository.existsByStudentId(1L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> studentService.deleteStudent(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya que tiene historial academico o inscripciones");
    }

    @Test
    void whenRegisterStudent_thenReturnSavedStudent() {
        //Given
        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("maria.gomez@usach.cl")).thenReturn(false);
        when(careerRepository.findById(10L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerIdAndStatus(10L, StudyPlanStatus.VIGENTE)).thenReturn(Optional.of(studyPlan));
        when(keycloakService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("keycloak-id");
        when(studentRepository.save(any(StudentEntity.class))).thenReturn(student);

        //When
        StudentEntity result = studentService.registerStudent(studentDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StudentStatus.REGULAR);
    }

    @Test
    void whenRegisterStudentCareerInactive_thenThrowException() {
        //Given
        career.setStatus(CareerStatus.INACTIVO);
        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("maria.gomez@usach.cl")).thenReturn(false);
        when(careerRepository.findById(10L)).thenReturn(Optional.of(career));

        //When / Then
        assertThatThrownBy(() -> studentService.registerStudent(studentDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya que esta inactiva");
    }

    @Test
    void whenRegisterStudentStudyPlanNotFound_thenThrowException() {
        //Given
        when(studentRepository.existsByRun("12345678-9")).thenReturn(false);
        when(studentRepository.existsByEmail("maria.gomez@usach.cl")).thenReturn(false);
        when(careerRepository.findById(10L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerIdAndStatus(10L, StudyPlanStatus.VIGENTE)).thenReturn(Optional.empty());

        //When / Then
        assertThatThrownBy(() -> studentService.registerStudent(studentDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No existe un plan de estudio vigente");
    }

    @Test
    void whenRegisterStudentDbFails_thenThrowExceptionAndRollbackKeycloak() {
        //Given
        when(studentRepository.existsByRun(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(careerRepository.findById(10L)).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerIdAndStatus(10L, StudyPlanStatus.VIGENTE)).thenReturn(Optional.of(studyPlan));
        when(keycloakService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("keycloak-id");
        when(studentRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        //When / Then
        assertThatThrownBy(() -> studentService.registerStudent(studentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al registrar estudiante en la base de datos");
        verify(keycloakService).deleteUser("keycloak-id");
    }

    @Test
    void whenUpdateStudentStatusValid_thenUpdateSuccessfully() {
        //Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        //When
        studentService.updateStudentStatus(1L, StudentStatus.APLAZADO);

        //Then
        assertThat(student.getStatus()).isEqualTo(StudentStatus.APLAZADO);
        verify(studentRepository).save(student);
    }
}