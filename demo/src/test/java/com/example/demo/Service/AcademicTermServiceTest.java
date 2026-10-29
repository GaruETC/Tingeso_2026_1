package com.example.demo.Service;

import com.example.demo.DTO.AcademicTermDto;
import com.example.demo.Entity.*;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Repository.*;
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
class AcademicTermServiceTest {

    @Mock
    private AcademicTermRepository academicTermRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AcademicHistorialRepository academicHistorialRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private AcademicTermService academicTermService;

    private AcademicTermEntity activeTerm;
    private AcademicTermDto termDto;
    private RegistrationEntity registration;
    private SectionEntity section;
    private StudentEntity student;

    @BeforeEach
    void setUp() {
        activeTerm = new AcademicTermEntity();
        activeTerm.setId(1L);
        activeTerm.setPeriod("2026-2");
        activeTerm.setStatus(AcademicTermStatus.ABIERTO);

        termDto = new AcademicTermDto(2026, 2);

        registration = new RegistrationEntity();
        registration.setId(10L);
        registration.setStudentId(100L);
        registration.setSectionId(50L);
        registration.setGrade(5.5f);

        section = new SectionEntity();
        section.setId(50L);
        section.setSubjectId(30L);

        student = new StudentEntity();
        student.setId(100L);
        student.setStudyPlanId(200L);
        student.setStatus(StudentStatus.REGULAR);
    }

    @Test
    void whenGetAcademicTerms_thenReturnAllTerms() {
        //Given
        when(academicTermRepository.findAll()).thenReturn(List.of(activeTerm));

        //When
        List<AcademicTermEntity> result = academicTermService.getAcademicTerms();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPeriod()).isEqualTo("2026-2");
    }

    @Test
    void whenGetTermsByStatus_thenReturnFilteredTerms() {
        //Given
        when(academicTermRepository.findByStatus(AcademicTermStatus.ABIERTO)).thenReturn(List.of(activeTerm));

        //When
        List<AcademicTermEntity> result = academicTermService.getTermsByStatus(AcademicTermStatus.ABIERTO);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(AcademicTermStatus.ABIERTO);
    }

    @Test
    void whenGetAcademicTermById_thenReturnTerm() {
        //Given
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));

        //When
        AcademicTermEntity result = academicTermService.getAcademicTermById(1L);

        //Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetAcademicTermByPeriod_thenReturnTerm() {
        //Given
        when(academicTermRepository.findByPeriod("2026-2")).thenReturn(Optional.of(activeTerm));

        //When
        AcademicTermEntity result = academicTermService.getAcademicTermByPeriod("2026-2");

        //Then
        assertThat(result.getPeriod()).isEqualTo("2026-2");
    }

    @Test
    void whenGetInactiveTerms_thenReturnList() {
        //Given
        when(academicTermRepository.findByStatus(AcademicTermStatus.CERRADO)).thenReturn(List.of(activeTerm));

        //When
        List<AcademicTermEntity> result = academicTermService.getInactiveTerms();

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenGetActiveTerm_thenReturnOptional() {
        //Given
        when(academicTermRepository.findFirstByStatus(AcademicTermStatus.ABIERTO)).thenReturn(Optional.of(activeTerm));

        //When
        Optional<AcademicTermEntity> result = academicTermService.getActiveTerm();

        //Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void whenCreateAcademicTerm_thenReturnSavedTerm() {
        //Given
        when(academicTermRepository.existsByPeriod("2026-2")).thenReturn(false);
        when(academicTermRepository.existsByStatus(AcademicTermStatus.ABIERTO)).thenReturn(false);
        when(academicTermRepository.save(any(AcademicTermEntity.class))).thenReturn(activeTerm);

        //When
        AcademicTermEntity result = academicTermService.createAcademicTerm(termDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AcademicTermStatus.ABIERTO);
    }

    @Test
    void whenCloseAcademicTermMissingGrades_thenThrowException() {
        //Given
        registration.setGrade(null);
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.findByAcademicTermId(1L)).thenReturn(List.of(registration));

        //When / Then
        assertThatThrownBy(() -> academicTermService.closeAcademicTerm(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Existen calificaciones faltantes en las siguientes secciones: Seccion 50");
    }

    @Test
    void whenCloseAcademicTermStudentPassesButNotGraduates_thenCloseSuccessfully() {
        //Given
        registration.setGrade(5.0f);
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.findByAcademicTermId(1L)).thenReturn(List.of(registration));
        when(sectionRepository.findById(50L)).thenReturn(Optional.of(section));
        when(studentService.getStudentById(100L)).thenReturn(student);
        when(subjectRepository.countByStudyPlanId(200L)).thenReturn(10);
        when(academicHistorialRepository.countByStudentIdAndStatus(100L, AcademicHistorialStatus.APROBADO)).thenReturn(5);

        //When
        academicTermService.closeAcademicTerm(1L);

        //Then
        assertThat(activeTerm.getStatus()).isEqualTo(AcademicTermStatus.CERRADO);
        verify(academicHistorialRepository).save(any(AcademicHistorialEntity.class));
        verify(academicTermRepository).save(activeTerm);
        verify(studentRepository, never()).save(any(StudentEntity.class));
    }

    @Test
    void whenCloseAcademicTermStudentPassesAndGraduates_thenCloseSuccessfully() {
        //Given
        registration.setGrade(4.0f);
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.findByAcademicTermId(1L)).thenReturn(List.of(registration));
        when(sectionRepository.findById(50L)).thenReturn(Optional.of(section));
        when(studentService.getStudentById(100L)).thenReturn(student);
        when(subjectRepository.countByStudyPlanId(200L)).thenReturn(10);
        when(academicHistorialRepository.countByStudentIdAndStatus(100L, AcademicHistorialStatus.APROBADO)).thenReturn(10);

        //When
        academicTermService.closeAcademicTerm(1L);

        //Then
        assertThat(student.getStatus()).isEqualTo(StudentStatus.EGRESADO);
        verify(studentRepository).save(student);
        verify(academicTermRepository).save(activeTerm);
    }

    @Test
    void whenCloseAcademicTermStudentFailsButNotEliminated_thenCloseSuccessfully() {
        //Given
        registration.setGrade(3.5f);
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.findByAcademicTermId(1L)).thenReturn(List.of(registration));
        when(sectionRepository.findById(50L)).thenReturn(Optional.of(section));
        when(studentService.getStudentById(100L)).thenReturn(student);
        when(academicHistorialRepository.countByStudentIdAndSubjectIdAndStatus(100L, 30L, AcademicHistorialStatus.REPROBADO)).thenReturn(1);

        //When
        academicTermService.closeAcademicTerm(1L);

        //Then
        verify(academicHistorialRepository).save(any(AcademicHistorialEntity.class));
        verify(academicTermRepository).save(activeTerm);
        verify(studentRepository, never()).save(any(StudentEntity.class));
    }

    @Test
    void whenCloseAcademicTermStudentFailsAndIsEliminated_thenCloseSuccessfully() {
        //Given
        registration.setGrade(2.0f);
        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.findByAcademicTermId(1L)).thenReturn(List.of(registration));
        when(sectionRepository.findById(50L)).thenReturn(Optional.of(section));
        when(studentService.getStudentById(100L)).thenReturn(student);
        when(academicHistorialRepository.countByStudentIdAndSubjectIdAndStatus(100L, 30L, AcademicHistorialStatus.REPROBADO)).thenReturn(2);

        //When
        academicTermService.closeAcademicTerm(1L);

        //Then
        assertThat(student.getStatus()).isEqualTo(StudentStatus.ELIMINADO);
        verify(studentRepository).save(student);
        verify(academicTermRepository).save(activeTerm);
    }
}