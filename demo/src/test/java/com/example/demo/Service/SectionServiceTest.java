package com.example.demo.Service;

import com.example.demo.DTO.SectionDto;
import com.example.demo.Entity.AcademicTermEntity;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.SectionEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Repository.RegistrationRepository;
import com.example.demo.Repository.SectionRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private TeacherService teacherService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private AcademicTermService academicTermService;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private SectionService sectionService;

    private SectionEntity section;
    private SectionDto sectionDto;
    private TeacherEntity teacher;
    private SubjectEntity subject;
    private AcademicTermEntity term;

    @BeforeEach
    void setUp() {
        teacher = new TeacherEntity();
        teacher.setId(1L);
        teacher.setStatus(TeacherStatus.ACTIVO);

        subject = new SubjectEntity();
        subject.setId(2L);
        subject.setTEL("1-1-0");

        term = new AcademicTermEntity();
        term.setId(3L);
        term.setStatus(AcademicTermStatus.ABIERTO);

        section = new SectionEntity();
        section.setId(10L);
        section.setTeacherId(1L);
        section.setSubjectId(2L);
        section.setAcademicTermId(3L);
        section.setSchedule(List.of("L1"));
        section.setMaxCapacity(30);

        sectionDto = new SectionDto(
                1L, 2L, 3L, List.of("L1"), 30);
    }

    @Test
    void whenGetSections_thenReturnAllSections() {
        //Given
        when(sectionRepository.findAll()).thenReturn(List.of(section));

        //When
        List<SectionEntity> result = sectionService.getSections();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(10L);
    }

    @Test
    void whenGetSectionsByTeacherIdAndAcademicTermId_thenReturnSections() {
        //Given
        when(sectionRepository.findByTeacherIdAndAcademicTermId(anyLong(), anyLong())).thenReturn(List.of(section));

        //When
        List<SectionEntity> result = sectionService.getSectionsByTeacherIdAndAcademicTermId(1L, 3L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTeacherId()).isEqualTo(1L);
    }

    @Test
    void whenGetSectionById_thenReturnSection() {
        //Given
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));

        //When
        SectionEntity result = sectionService.getSectionById(10L);

        //Then
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void whenGetSectionsByAcademicTermId_thenReturnSections() {
        //Given
        when(sectionRepository.findByAcademicTermId(3L)).thenReturn(List.of(section));

        //When
        List<SectionEntity> result = sectionService.getSectionsByAcademicTermId(3L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAcademicTermId()).isEqualTo(3L);
    }

    @Test
    void whenGetSectionsByTeacherId_thenReturnSections() {
        //Given
        when(sectionRepository.findByTeacherId(1L)).thenReturn(List.of(section));

        //When
        List<SectionEntity> result = sectionService.getSectionsByTeacherId(1L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTeacherId()).isEqualTo(1L);
    }

    @Test
    void whenGetSectionBySubjectId_thenReturnSection() {
        //Given
        when(sectionRepository.findBySubjectId(2L)).thenReturn(Optional.of(section));

        //When
        SectionEntity result = sectionService.getSectionBySubjectId(2L);

        //Then
        assertThat(result.getSubjectId()).isEqualTo(2L);
    }

    @Test
    void whenCreateSection_thenReturnSavedSection() {
        //Given
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(false);
        when(sectionRepository.findByTeacherIdAndAcademicTermId(1L, 3L)).thenReturn(List.of());
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(section);

        //When
        SectionEntity result = sectionService.createSection(sectionDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getMaxCapacity()).isEqualTo(30);
    }

    @Test
    void whenCreateSectionTeacherInactive_thenThrowException() {
        //Given
        teacher.setStatus(TeacherStatus.INACTIVO);
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(sectionDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El profesor asociado debe encontrarse activo");
    }

    @Test
    void whenCreateSectionAlreadyExistsInTerm_thenThrowException() {
        //Given
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(sectionDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe una seccion asociada a esta asignatura en este periodo academico");
    }

    @Test
    void whenCreateSectionTermClosed_thenThrowException() {
        //Given
        term.setStatus(AcademicTermStatus.CERRADO);
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(sectionDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Este periodo academico esta cerrado");
    }

    @Test
    void whenCreateSectionCapacityZero_thenThrowException() {
        //Given
        SectionDto badCapacityDto = new SectionDto(1L, 2L, 3L, List.of("L1"), 0);
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(badCapacityDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La capacidad maxima debe ser de 1 a 60 estudiantes");
    }

    @Test
    void whenCreateSectionCapacityGreaterThan60_thenThrowException() {
        //Given
        SectionDto badCapacityDto = new SectionDto(1L, 2L, 3L, List.of("L1"), 61);
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(badCapacityDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La capacidad maxima debe ser de 1 a 60 estudiantes");
    }

    @Test
    void whenCreateSectionTelMismatch_thenThrowException() {
        //Given
        subject.setTEL("2-2-0");
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getAcademicTermById(3L)).thenReturn(term);
        when(sectionRepository.existsBySubjectIdAndAcademicTermId(2L, 3L)).thenReturn(false);

        //When / Then
        assertThatThrownBy(() -> sectionService.createSection(sectionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Las horas de modulos de la seccion con coinciden con el TEL");
    }

    @Test
    void whenValidateScheduleEmpty_thenThrowException() {
        //When / Then
        assertThatThrownBy(() -> sectionService.validateSchedule(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La seccion debe tener al menos un bloque");
    }

    @Test
    void whenValidateScheduleInvalidFormat_thenThrowException() {
        //When / Then
        assertThatThrownBy(() -> sectionService.validateSchedule(List.of("Z9")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Al menos uno de los modulos tiene el formato incorrecto");
    }

    @Test
    void whenValidateScheduleDuplicate_thenThrowException() {
        //When / Then
        assertThatThrownBy(() -> sectionService.validateSchedule(List.of("L1", "L1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La seccion contiene modulos duplicados");
    }

    @Test
    void whenDeleteSection_thenVerifyDeletion() {
        //Given
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(registrationRepository.existsBySectionId(10L)).thenReturn(false);

        //When
        sectionService.deleteSection(10L);

        //Then
        verify(sectionRepository).delete(section);
    }
}