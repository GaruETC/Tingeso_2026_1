package com.example.demo.Service;

import com.example.demo.DTO.RegistrationDto;
import com.example.demo.DTO.SubmitGradesDto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private SectionService sectionService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private AcademicTermService academicTermService;

    @Mock
    private AcademicHistorialRepository academicHistorialRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationEntity registration;
    private RegistrationDto registrationDto;
    private StudentEntity student;
    private SectionEntity section;
    private SubjectEntity subject;
    private AcademicTermEntity activeTerm;

    @BeforeEach
    void setUp() {
        student = new StudentEntity();
        student.setId(1L);
        student.setStatus(StudentStatus.REGULAR);
        student.setMatriculation(true);
        student.setStudyPlanId(100L);

        subject = new SubjectEntity();
        subject.setId(2L);
        subject.setStudyPlanId(100L);
        subject.setSemester(3);
        subject.setPrerequisiteIds(List.of());

        section = new SectionEntity();
        section.setId(3L);
        section.setSubjectId(2L);
        section.setAcademicTermId(10L);
        section.setMaxCapacity(30);
        section.setSchedule(List.of("L1", "M2"));

        activeTerm = new AcademicTermEntity();
        activeTerm.setId(10L);
        activeTerm.setStatus(AcademicTermStatus.ABIERTO);

        registration = new RegistrationEntity();
        registration.setId(1000L);
        registration.setStudentId(1L);
        registration.setSectionId(3L);
        registration.setAcademicTermId(10L);
        registration.setRegistrationDate(LocalDate.now());

        registrationDto = new RegistrationDto(1L, 3L);
    }

    @Test
    void whenGetRegistrations_thenReturnAll() {
        //Given
        when(registrationRepository.findAll()).thenReturn(List.of(registration));

        //When
        List<RegistrationEntity> result = registrationService.getRegistrations();

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenGetRegistrationsByStudentIdAndTermId_thenReturnList() {
        //Given
        when(registrationRepository.findByStudentIdAndAcademicTermId(1L, 10L)).thenReturn(List.of(registration));

        //When
        List<RegistrationEntity> result = registrationService.getRegistrationsByStudentIdAndTermId(1L, 10L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStudentId()).isEqualTo(1L);
    }

    @Test
    void whenGetRegistrationById_thenReturnRegistration() {
        //Given
        when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

        //When
        RegistrationEntity result = registrationService.getRegistrationById(1000L);

        //Then
        assertThat(result.getId()).isEqualTo(1000L);
    }

    @Test
    void whenGetRegistrationByIdNotFound_thenThrowException() {
        //Given
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        //When / Then
        assertThatThrownBy(() -> registrationService.getRegistrationById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontro la inscripcion con id");
    }

    @Test
    void whenGetRegistrationByStudentId_thenReturnList() {
        //Given
        when(registrationRepository.findByStudentId(1L)).thenReturn(List.of(registration));

        //When
        List<RegistrationEntity> result = registrationService.getRegistrationByStudentId(1L);

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenGetRegistrationsBySectionId_thenReturnList() {
        //Given
        when(registrationRepository.findBySectionId(3L)).thenReturn(List.of(registration));

        //When
        List<RegistrationEntity> result = registrationService.getRegistrationsBySectionId(3L);

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenGetRegistrationByAcademicTermId_thenReturnList() {
        //Given
        when(registrationRepository.findByAcademicTermId(10L)).thenReturn(List.of(registration));

        //When
        List<RegistrationEntity> result = registrationService.getRegistrationByAcademicTermId(10L);

        //Then
        assertThat(result).hasSize(1);
    }

    @Test
    void whenCreateRegistrationValidData_thenReturnSavedRegistration() {
        //Given
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));
        when(registrationRepository.existsByStudentIdAndSectionId(1L, 3L)).thenReturn(false);
        when(registrationRepository.countBySectionId(3L)).thenReturn(10L);
        when(academicHistorialRepository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(List.of());
        when(subjectRepository.findByStudyPlanIdAndSemesterLessThan(100L, 3)).thenReturn(List.of());
        when(registrationRepository.findByStudentIdAndAcademicTermId(1L, 10L)).thenReturn(List.of());
        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(registration);

        //When
        RegistrationEntity result = registrationService.createRegistration(registrationDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(1L);
        assertThat(section.getMaxCapacity()).isEqualTo(29);
    }

    @Test
    void whenCreateRegistrationNoActiveTerm_thenThrowException() {
        //Given
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.empty());

        //When / Then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No hay periodo academico activo en este momento");
    }

    @Test
    void whenCreateRegistrationStudentNotRegular_thenThrowException() {
        //Given
        student.setStatus(StudentStatus.APLAZADO);
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));

        //When / Then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("el estudiante debe estar en estado REGULAR");
    }

    @Test
    void whenCreateRegistrationStudentNotMatriculated_thenThrowException() {
        //Given
        student.setMatriculation(false);
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));

        //When / Then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("el estudiante debe estar matriculado");
    }

    @Test
    void whenCreateRegistrationSectionNotInActiveTerm_thenThrowException() {
        //Given
        section.setAcademicTermId(99L);
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));

        //When / Then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no es parte del periodo academico activo");
    }

    @Test
    void whenCreateRegistrationSubjectWrongStudyPlan_thenThrowException() {
        //Given
        subject.setStudyPlanId(99L);
        when(studentService.getStudentById(1L)).thenReturn(student);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));

        //When / Then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no pertenece al plan de estudios del estudiante");
    }

    @Test
    void whenValidateSubjectApprovalsAlreadyApproved_thenThrowException() {
        //Given
        AcademicHistorialEntity record = new AcademicHistorialEntity();
        record.setStatus(AcademicHistorialStatus.APROBADO);
        when(academicHistorialRepository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(List.of(record));

        //When / Then
        assertThatThrownBy(() -> registrationService.validateSubjectApprovals(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue aprovada");
    }

    @Test
    void whenValidateApprovedPrereqApproved_thenReturnTrue() {
        //Given
        subject.setPrerequisiteIds(List.of(9L));
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicHistorialRepository.existsByStudentIdAndSubjectIdAndStatus(1L, 9L, AcademicHistorialStatus.APROBADO)).thenReturn(true);

        //When
        boolean result = registrationService.validateApprovedPrereq(1L, 2L);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void whenValidateApprovedPrereqNotApproved_thenReturnFalse() {
        //Given
        subject.setPrerequisiteIds(List.of(9L));
        when(subjectService.getSubjectById(2L)).thenReturn(subject);
        when(academicHistorialRepository.existsByStudentIdAndSubjectIdAndStatus(1L, 9L, AcademicHistorialStatus.APROBADO)).thenReturn(false);

        //When
        boolean result = registrationService.validateApprovedPrereq(1L, 2L);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void whenValidateNoLowerSemesterWithPendingSubject_thenThrowException() {
        //Given
        SubjectEntity lowerSubject = new SubjectEntity();
        lowerSubject.setId(9L);
        lowerSubject.setPrerequisiteIds(List.of());

        SectionEntity lowerSection = new SectionEntity();
        lowerSection.setId(99L);

        when(subjectRepository.findByStudyPlanIdAndSemesterLessThan(100L, 3)).thenReturn(List.of(lowerSubject));
        when(academicHistorialRepository.existsByStudentIdAndSubjectIdAndStatus(1L, 9L, AcademicHistorialStatus.APROBADO)).thenReturn(false);
        when(sectionRepository.findBySubjectIdAndAcademicTermId(9L, 10L)).thenReturn(Optional.of(lowerSection));
        when(registrationRepository.existsByStudentIdAndSectionId(1L, 99L)).thenReturn(false);
        when(subjectService.getSubjectById(9L)).thenReturn(lowerSubject);

        //When / Then
        assertThatThrownBy(() -> registrationService.validateNoLowerSemester(student, subject, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ramos pendientes de semestres inferiores disponibles");
    }

    @Test
    void whenValidateStudentScheduleConflict_thenThrowException() {
        //Given
        RegistrationEntity existingReg = new RegistrationEntity();
        existingReg.setSectionId(5L);

        SectionEntity existingSection = new SectionEntity();
        existingSection.setSchedule(List.of("L1", "J4"));

        when(registrationRepository.findByStudentIdAndAcademicTermId(1L, 10L)).thenReturn(List.of(existingReg));
        when(sectionService.getSectionById(5L)).thenReturn(existingSection);

        List<String> newSchedule = List.of("L1", "M3");

        //When / Then
        assertThatThrownBy(() -> registrationService.validateStudentSchedule(1L, 10L, newSchedule))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Conflicto de horario: ya tienes clases inscritas en el módulo: L1");
    }

    @Test
    void whenCancelRegistration_thenVerifyDeletion() {
        //Given
        when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));
        when(academicTermService.getActiveTerm()).thenReturn(Optional.of(activeTerm));
        when(sectionService.getSectionById(3L)).thenReturn(section);

        //When
        registrationService.cancelRegistration(1000L);

        //Then
        assertThat(section.getMaxCapacity()).isEqualTo(31);
        verify(registrationRepository).delete(registration);
    }

    @Test
    void whenSubmitGradesValidData_thenSaveGrade() {
        //Given
        SubmitGradesDto.StudentGradeDto gradeDto = new SubmitGradesDto.StudentGradeDto(1000L, 5.5f);
        SubmitGradesDto submitDto = new SubmitGradesDto(50L, List.of(gradeDto));

        section.setTeacherId(50L);
        when(sectionService.getSectionById(3L)).thenReturn(section);
        when(academicTermService.getAcademicTermById(10L)).thenReturn(activeTerm);
        when(registrationRepository.findById(1000L)).thenReturn(Optional.of(registration));

        //When
        registrationService.submitGrades(3L, submitDto);

        //Then
        assertThat(registration.getGrade()).isEqualTo(5.5f);
        verify(registrationRepository).save(registration);
    }

}