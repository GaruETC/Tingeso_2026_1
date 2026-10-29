package com.example.demo.Service;

import com.example.demo.DTO.TeacherDto;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Repository.SectionRepository;
import com.example.demo.Repository.TeacherRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {
    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AcademicTermService academicTermService;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private TeacherService teacherService;

    private TeacherEntity teacher;
    private TeacherDto teacherDto;

    @BeforeEach
    void setUp() {
        teacher = new TeacherEntity();
        teacher.setId(1L);
        teacher.setRun("12345678-9");
        teacher.setEmail("juan.perez@usach.cl");
        teacher.setFullName("Juan Perez");
        teacher.setTitle("Ingeniero");
        teacher.setDegree("Magister");
        teacher.setStatus(TeacherStatus.ACTIVO);

        teacherDto = new TeacherDto(
                "12345678-9",
                "juan.perez@usach.cl",
                "Juan Perez",
                "Ingeniero",
                "Magister"
        );
    }

    @Test
    void whenGetTeachers_thenReturnAllTeachers() {
        //Given
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        //When
        List<TeacherEntity> result = teacherService.getTeachers();
        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFullName()).isEqualTo("Juan Perez");
    }

    @Test
    void whenGetTeachersByStatus_thenReturnFilteredTeachers() {
        //Given
        when(teacherRepository.findByStatus(TeacherStatus.ACTIVO)).thenReturn(List.of(teacher));

        //When
        List<TeacherEntity> result = teacherService.getTeachersByStatus(TeacherStatus.ACTIVO);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(TeacherStatus.ACTIVO);
    }

    @Test
    void whenGetActiveTeachers_thenReturnActiveTeachers() {
        //Given
        when(teacherRepository.findByStatus(TeacherStatus.ACTIVO)).thenReturn(List.of(teacher));

        //When
        List<TeacherEntity> result = teacherService.getActiveTeachers();

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenGetTeacherById_thenReturnTeacher() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        //When
        TeacherEntity result = teacherService.getTeacherById(1L);

        //Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("juan.perez@usach.cl");
    }

    @Test
    void whenGetTeacherByEmail_thenReturnTeacher() {
        //Given
        when(teacherRepository.findByEmail("juan.perez@usach.cl")).thenReturn(Optional.of(teacher));

        //When
        TeacherEntity result = teacherService.getTeacherByEmail("juan.perez@usach.cl");

        //Then
        assertThat(result.getEmail()).isEqualTo("juan.perez@usach.cl");
    }

    @Test
    void whenGetTeacherByRun_thenReturnTeacher() {
        //Given
        when(teacherRepository.findByRun("12345678-9")).thenReturn(Optional.of(teacher));

        //When
        TeacherEntity result = teacherService.getTeacherByRun("12345678-9");

        //Then
        assertThat(result.getRun()).isEqualTo("12345678-9");
    }

    @Test
    void whenGetTeacherByFullName_thenReturnTeacher() {
        //Given
        when(teacherRepository.findByFullName("Juan Perez")).thenReturn(Optional.of(teacher));

        //When
        TeacherEntity result = teacherService.getTeacherByFullName("Juan Perez");

        //Then
        assertThat(result.getFullName()).isEqualTo("Juan Perez");
    }

    @Test
    void whenRegisterTeacher_thenReturnSavedTeacher() {
        //Given
        when(teacherRepository.existsByRun("12345678-9")).thenReturn(false);
        when(teacherRepository.existsByEmail("juan.perez@usach.cl")).thenReturn(false);
        when(keycloakService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("keycloak-id-123");
        when(teacherRepository.save(any(TeacherEntity.class))).thenReturn(teacher);

        //When
        TeacherEntity result = teacherService.registerTeacher(teacherDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TeacherStatus.ACTIVO);
    }

    @Test
    void whenRegisterTeacherExistingRun_thenThrowException() {
        //Given
        when(teacherRepository.existsByRun("12345678-9")).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> teacherService.registerTeacher(teacherDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya se encuentra registrado en el sistema el run");
    }

    @Test
    void whenRegisterTeacherExistingEmail_thenThrowException() {
        //Given
        when(teacherRepository.existsByRun("12345678-9")).thenReturn(false);
        when(teacherRepository.existsByEmail("juan.perez@usach.cl")).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> teacherService.registerTeacher(teacherDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya se encuentra registrado en el sistema el email");
    }

    @Test
    void whenRegisterTeacherBlankName_thenThrowException() {
        //Given
        TeacherDto badDto = new TeacherDto("12345678-9", "juan.perez@usach.cl", "   ", "Ingeniero", "Magister");

        //When / Then
        assertThatThrownBy(() -> teacherService.registerTeacher(badDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El nombre completo del docente es obligatorio");
    }

    @Test
    void whenUpdateTeacher_thenReturnUpdatedTeacher() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByRunAndIdNot("12345678-9", 1L)).thenReturn(false);
        when(teacherRepository.existsByEmailAndIdNot("juan.perez@usach.cl", 1L)).thenReturn(false);
        when(teacherRepository.save(any(TeacherEntity.class))).thenReturn(teacher);

        //When
        TeacherEntity result = teacherService.updateTeacher(1L, teacherDto);

        //Then
        assertThat(result.getFullName()).isEqualTo("Juan Perez");
    }

    @Test
    void whenUpdateTeacherExistingEmail_thenThrowException() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByRunAndIdNot("12345678-9", 1L)).thenReturn(false);
        when(teacherRepository.existsByEmailAndIdNot("juan.perez@usach.cl", 1L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> teacherService.updateTeacher(1L, teacherDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya se encuentra registrado en el sistema el email");
    }

    @Test
    void whenUpdateTeacherStatus_thenUpdateSuccessfully() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        //When
        teacherService.updateTeacherStatus(1L, TeacherStatus.INACTIVO);

        //Then
        verify(teacherRepository).save(teacher);
    }

    @Test
    void whenDeleteTeacher_thenVerifyDeletion() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherId(1L)).thenReturn(false);

        //When
        teacherService.deleteTeacher(1L);

        //Then
        verify(teacherRepository).delete(teacher);
    }

    @Test
    void whenDeleteTeacherWithSections_thenThrowException() {
        //Given
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherId(1L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> teacherService.deleteTeacher(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya que tiene secciones asociadas");
    }
}