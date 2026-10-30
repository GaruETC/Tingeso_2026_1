package com.example.demo.Integration;

import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.Entity.*;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"ADMIN"})
class AcademicTermIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AcademicTermRepository academicTermRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicHistorialRepository academicHistorialRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AcademicTermEntity activeTerm;
    private StudentEntity activeStudent;
    private SectionEntity activeSection;

    @BeforeEach
    void setUp() {
        academicHistorialRepository.deleteAll();
        registrationRepository.deleteAll();
        sectionRepository.deleteAll();
        subjectRepository.deleteAll();
        studentRepository.deleteAll();
        studyPlanRepository.deleteAll();
        academicTermRepository.deleteAll();

        activeTerm = new AcademicTermEntity();
        activeTerm.setPeriod("2026-2");
        activeTerm.setStatus(AcademicTermStatus.ABIERTO);
        activeTerm = academicTermRepository.save(activeTerm);

        StudyPlanEntity plan = new StudyPlanEntity();
        plan.setCareerId(1L);
        plan.setCode("PLAN-1");
        plan.setStatus(StudyPlanStatus.VIGENTE);
        plan = studyPlanRepository.save(plan);

        SubjectEntity subject = new SubjectEntity();
        subject.setCode("CS101");
        subject.setStudyPlanId(plan.getId());
        subject = subjectRepository.save(subject);

        activeSection = new SectionEntity();
        activeSection.setTeacherId(1L);
        activeSection.setSubjectId(subject.getId());
        activeSection.setAcademicTermId(activeTerm.getId());
        activeSection.setMaxCapacity(30);
        activeSection.setSchedule(List.of("L1, W1, V1"));
        activeSection = sectionRepository.save(activeSection);

        activeStudent = new StudentEntity();
        activeStudent.setRun("11111111-1");
        activeStudent.setEmail("juan.perez@usach.cl");
        activeStudent.setFullName("Juan Perez");
        activeStudent.setMatriculation(true);
        activeStudent.setStatus(StudentStatus.REGULAR);
        activeStudent.setStudyPlanId(plan.getId());
        activeStudent.setCareerId(1L);
        activeStudent = studentRepository.save(activeStudent);
    }

    // 3 SUCCESS

    @Test
    void whenCloseTermWithoutRegistrations_thenTermIsClosed() throws Exception {
        // When / Then
        mockMvc.perform(patch("/api/academic-terms/" + activeTerm.getId() + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify
        AcademicTermEntity updatedTerm = academicTermRepository.findById(activeTerm.getId()).orElseThrow();
        assertThat(updatedTerm.getStatus()).isEqualTo(AcademicTermStatus.CERRADO);
    }

    @Test
    void whenCloseTermWithApprovedGrade_thenHistorialIsAprobado() throws Exception {
        // Given
        RegistrationEntity registration = new RegistrationEntity();
        registration.setStudentId(activeStudent.getId());
        registration.setAcademicTermId(activeTerm.getId());
        registration.setSectionId(activeSection.getId());
        registration.setGrade(6.5f);
        registrationRepository.save(registration);

        // When / Then
        mockMvc.perform(patch("/api/academic-terms/" + activeTerm.getId() + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify
        List<AcademicHistorialEntity> historial = academicHistorialRepository.findByStudentId(activeStudent.getId());
        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().getStatus()).isEqualTo(AcademicHistorialStatus.APROBADO);
    }

    @Test
    void whenCloseTermWithFailingGrade_thenHistorialIsReprobado() throws Exception {

        // Given
        RegistrationEntity registration = new RegistrationEntity();
        registration.setStudentId(activeStudent.getId());
        registration.setAcademicTermId(activeTerm.getId());
        registration.setSectionId(activeSection.getId());
        registration.setGrade(3.5f); // Nota < 4.0
        registrationRepository.save(registration);

        // When / Then
        mockMvc.perform(patch("/api/academic-terms/" + activeTerm.getId() + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify
        List<AcademicHistorialEntity> historial = academicHistorialRepository.findByStudentId(activeStudent.getId());
        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().getStatus()).isEqualTo(AcademicHistorialStatus.REPROBADO);
    }

    // 3 FAILED

    @Test
    void whenCreateTermWhileAnotherIsOpen_thenConflict() throws Exception {
        String newTermDtoJson = """
                {
                    "year": 2027,
                    "semester": 1
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/academic-terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTermDtoJson))
                .andExpect(status().isBadRequest());

        // Verify
        assertThat(academicTermRepository.findAll()).hasSize(1);
    }

    @Test
    void whenCloseAlreadyClosedTerm_thenConflict() throws Exception {
        // Given
        activeTerm.setStatus(AcademicTermStatus.CERRADO);
        academicTermRepository.save(activeTerm);

        // When / Then
        mockMvc.perform(patch("/api/academic-terms/" + activeTerm.getId() + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify
        AcademicTermEntity termInDb = academicTermRepository.findById(activeTerm.getId()).orElseThrow();
        assertThat(termInDb.getStatus()).isEqualTo(AcademicTermStatus.CERRADO);
    }

    @Test
    void whenCloseTermWithPendingGrades_thenConflict() throws Exception {
        // Given
        RegistrationEntity registration = new RegistrationEntity();
        registration.setStudentId(activeStudent.getId());
        registration.setAcademicTermId(activeTerm.getId());
        registration.setSectionId(activeSection.getId());
        registration.setGrade(null);
        registrationRepository.save(registration);
        // When / Then
        mockMvc.perform(patch("/api/academic-terms/" + activeTerm.getId() + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify
        AcademicTermEntity termInDb = academicTermRepository.findById(activeTerm.getId()).orElseThrow();
        assertThat(termInDb.getStatus()).isEqualTo(AcademicTermStatus.ABIERTO);
        assertThat(academicHistorialRepository.findAll()).isEmpty();
    }
}