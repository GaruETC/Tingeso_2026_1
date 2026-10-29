package com.example.demo.Service;

import com.example.demo.DTO.SubjectDto;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Repository.AcademicHistorialRepository;
import com.example.demo.Repository.SubjectRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private AcademicHistorialRepository academicHistorialRepository;

    @InjectMocks
    private SubjectService subjectService;

    private SubjectEntity subject;
    private SubjectDto subjectDto;

    @BeforeEach
    void setUp() {
        subject = new SubjectEntity();
        subject.setId(1L);
        subject.setCode("CS101");
        subject.setName("Programacion");
        subject.setTEL("2-2-2");
        subject.setSCT(5);
        subject.setSemester(2);
        subject.setStudyPlanId(10L);
        subject.setPrerequisiteIds(List.of());

        subjectDto = new SubjectDto(
                "CS101",
                "Programacion",
                2,
                2,
                2,
                5,
                2,
                List.of(),
                10L
        );
    }

    @Test
    void whenGetSubjects_thenReturnAllSubjects() {
        //Given
        when(subjectRepository.findAll()).thenReturn(List.of(subject));

        //When
        List<SubjectEntity> result = subjectService.getSubjects();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCode()).isEqualTo("CS101");
    }

    @Test
    void whenGetPrerequisitesNull_thenReturnEmptyList() {
        //Given
        subject.setPrerequisiteIds(null);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        //When
        List<SubjectEntity> result = subjectService.getPrerequisites(1L);

        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void whenGetPrerequisitesExist_thenReturnPrerequisitesList() {
        //Given
        subject.setPrerequisiteIds(List.of(2L));
        SubjectEntity prereq = new SubjectEntity();
        prereq.setId(2L);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.findAllById(List.of(2L))).thenReturn(List.of(prereq));

        //When
        List<SubjectEntity> result = subjectService.getPrerequisites(1L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(2L);
    }

    @Test
    void whenGetSubjectById_thenReturnSubject() {
        //Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        //When
        SubjectEntity result = subjectService.getSubjectById(1L);

        //Then
        assertThat(result.getCode()).isEqualTo("CS101");
    }

    @Test
    void whenGetSubjectByCode_thenReturnSubject() {
        //Given
        when(subjectRepository.findByCode("CS101")).thenReturn(Optional.of(subject));

        //When
        SubjectEntity result = subjectService.getSubjectByCode("CS101");

        //Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetSubjectByName_thenReturnSubject() {
        //Given
        when(subjectRepository.findByName("Programacion")).thenReturn(Optional.of(subject));

        //When
        SubjectEntity result = subjectService.getSubjectByName("Programacion");

        //Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetSubjectsByStudyPlanId_thenReturnList() {
        //Given
        when(subjectRepository.findByStudyPlanId(10L)).thenReturn(List.of(subject));

        //When
        List<SubjectEntity> result = subjectService.getSubjectsByStudyPlanId(10L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStudyPlanId()).isEqualTo(10L);
    }

    @Test
    void whenRegisterSubject_thenReturnSavedSubject() {
        //Given
        when(subjectRepository.existsByCode("CS101")).thenReturn(false);
        when(subjectRepository.save(any(SubjectEntity.class))).thenReturn(subject);

        //When
        SubjectEntity result = subjectService.registerSubject(subjectDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CS101");
    }

    @Test
    void whenRegisterSubjectExistingCode_thenThrowException() {
        //Given
        when(subjectRepository.existsByCode("CS101")).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> subjectService.registerSubject(subjectDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya se encuentra registrada una asignatura con el código CS101");
    }

    @Test
    void whenUpdateSubject_thenReturnUpdatedSubject() {
        //Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByCodeAndIdNot("CS101", 1L)).thenReturn(false);
        when(subjectRepository.save(any(SubjectEntity.class))).thenReturn(subject);

        //When
        SubjectEntity result = subjectService.updateSubject(1L, subjectDto);

        //Then
        assertThat(result.getCode()).isEqualTo("CS101");
    }

    @Test
    void whenUpdateSubjectExistingCodeForOtherId_thenThrowException() {
        //Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByCodeAndIdNot("CS101", 1L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> subjectService.updateSubject(1L, subjectDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya se encuentra registrada una asignatura con el código CS101");
    }

    @Test
    void whenValidateTelWithTotalHours8OrMore_thenThrowException() {
        //When / Then
        assertThatThrownBy(() -> subjectService.validateTel(3, 3, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Las horas semanales no deben ser 8 o mayor");
    }

    @Test
    void whenValidateTelWithNegativeHours_thenThrowException() {
        //When / Then
        assertThatThrownBy(() -> subjectService.validateTel(-1, 2, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Las horas no pueden ser negativas");
    }

    @Test
    void whenRegisterSubjectInvalidSctZero_thenThrowException() {
        //Given
        SubjectDto badSctDto = new SubjectDto("CS101", "Prog", 2, 2, 2, 0, 2, List.of(), 10L);
        when(subjectRepository.existsByCode("CS101")).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> subjectService.registerSubject(badSctDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El SCT debe ser mayor a 0 o menor o igual a 7");
    }

    @Test
    void whenRegisterSubjectMoreThan3Prerequisites_thenThrowException() {
        //Given
        SubjectDto tooManyPrereqsDto = new SubjectDto("CS101", "Prog", 2, 2, 2, 5, 2, List.of(2L, 3L, 4L, 5L), 10L);
        when(subjectRepository.existsByCode("CS101")).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> subjectService.registerSubject(tooManyPrereqsDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Los prerequisitos solo pueden ser 3 como máximo");
    }

    @Test
    void whenRegisterSubjectPrerequisiteWrongSemester_thenThrowException() {
        //Given
        SubjectDto prereqDto = new SubjectDto("CS101", "Prog", 2, 2, 2, 5, 2, List.of(2L), 10L);
        SubjectEntity prereqEntity = new SubjectEntity();
        prereqEntity.setId(2L);
        prereqEntity.setStudyPlanId(10L);
        prereqEntity.setSemester(3);

        when(subjectRepository.existsByCode("CS101")).thenReturn(false);
        when(subjectRepository.findAllById(List.of(2L))).thenReturn(List.of(prereqEntity));

        //When / Then
        assertThatThrownBy(() -> subjectService.registerSubject(prereqDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe pertenecer a un semestre estrictamente anterior");
    }

    @Test
    void whenDeleteSubject_thenVerifyDeletion() {
        //Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicHistorialRepository.existsBySubjectId(1L)).thenReturn(false);

        //When
        subjectService.deleteSubject(1L);

        //Then
        verify(subjectRepository).delete(subject);
    }

    @Test
    void whenDeleteSubjectWithHistory_thenThrowException() {
        //Given
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(academicHistorialRepository.existsBySubjectId(1L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> subjectService.deleteSubject(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Esta asignatura forma parte de un historial academico");
    }
}