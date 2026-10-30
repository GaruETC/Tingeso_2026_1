package com.example.demo.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.DTO.RegistrationDto;
import com.example.demo.Entity.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"STUDENT"})
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AcademicTermRepository academicTermRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private StudentEntity activeStudent;
    private SectionEntity availableSection;
    private SectionEntity anotherAvailableSection;
    private AcademicTermEntity activeTerm;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        sectionRepository.deleteAll();
        subjectRepository.deleteAll();
        studentRepository.deleteAll();
        academicTermRepository.deleteAll();

        activeTerm = new AcademicTermEntity();
        activeTerm.setPeriod("2026-2");
        activeTerm.setStatus(AcademicTermStatus.ABIERTO);
        activeTerm = academicTermRepository.save(activeTerm);

        activeStudent = new StudentEntity();
        activeStudent.setRun("11111111-1");
        activeStudent.setEmail("juan.perez@usach.cl");
        activeStudent.setFullName("Juan Perez");
        activeStudent.setStatus(StudentStatus.REGULAR);
        activeStudent.setMatriculation(true);
        activeStudent.setCareerId(1L);
        activeStudent.setStudyPlanId(1L);
        activeStudent = studentRepository.save(activeStudent);

        SubjectEntity subject1 = new SubjectEntity();
        subject1.setCode("CS101");
        subject1.setStudyPlanId(1L);
        subject1.setSemester(1);
        subject1 = subjectRepository.save(subject1);

        SubjectEntity subject2 = new SubjectEntity();
        subject2.setCode("CS102");
        subject2.setStudyPlanId(1L);
        subject2.setSemester(1);
        subject2 = subjectRepository.save(subject2);

        availableSection = new SectionEntity();
        availableSection.setTeacherId(1L);
        availableSection.setSubjectId(subject1.getId());
        availableSection.setAcademicTermId(activeTerm.getId());
        availableSection.setMaxCapacity(30);
        availableSection.setSchedule(List.of("L1", "M2"));
        availableSection = sectionRepository.save(availableSection);

        anotherAvailableSection = new SectionEntity();
        anotherAvailableSection.setTeacherId(2L);
        anotherAvailableSection.setSubjectId(subject2.getId());
        anotherAvailableSection.setAcademicTermId(activeTerm.getId());
        anotherAvailableSection.setMaxCapacity(30);
        anotherAvailableSection.setSchedule(List.of("J3", "V4"));
        anotherAvailableSection = sectionRepository.save(anotherAvailableSection);
    }

    // 3 SUCCESS

    @Test
    void whenCreateRegistration_thenRegistrationIsStoredInDatabase() throws Exception {
        // Given
        RegistrationDto registrationDto = new RegistrationDto(activeStudent.getId(), availableSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(activeStudent.getId()))
                .andExpect(jsonPath("$.sectionId").value(availableSection.getId()));

        // Verify
        List<RegistrationEntity> saved = registrationRepository.findByStudentId(activeStudent.getId());
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getSectionId()).isEqualTo(availableSection.getId());
    }

    @Test
    void whenCreateMultipleRegistrationsWithoutConflict_thenBothAreStored() throws Exception {
        // Given
        RegistrationDto firstRegistration = new RegistrationDto(activeStudent.getId(), availableSection.getId());
        RegistrationDto secondRegistration = new RegistrationDto(activeStudent.getId(), anotherAvailableSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRegistration)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRegistration)))
                .andExpect(status().isCreated());

        // Verify
        List<RegistrationEntity> saved = registrationRepository.findByStudentId(activeStudent.getId());
        assertThat(saved).hasSize(2);
    }

    @Test
    void whenCreateRegistrationWithLastSeat_thenRegistrationIsStored() throws Exception {
        // Given
        availableSection.setMaxCapacity(1);
        sectionRepository.save(availableSection);

        RegistrationDto registrationDto = new RegistrationDto(activeStudent.getId(), availableSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated());

        // Verify
        SectionEntity updatedSection = sectionRepository.findById(availableSection.getId()).orElseThrow();
        assertThat(updatedSection.getMaxCapacity()).isZero();
    }

    // 3 FAILED

    @Test
    void whenCreateRegistrationInInactiveTerm_thenConflict() throws Exception {
        // Given
        AcademicTermEntity inactiveTerm = new AcademicTermEntity();
        inactiveTerm.setPeriod("2025-1");
        inactiveTerm.setStatus(AcademicTermStatus.CERRADO);
        inactiveTerm = academicTermRepository.save(inactiveTerm);

        SectionEntity inactiveSection = new SectionEntity();
        inactiveSection.setTeacherId(3L);
        inactiveSection.setSubjectId(availableSection.getSubjectId());
        inactiveSection.setAcademicTermId(inactiveTerm.getId());
        inactiveSection.setMaxCapacity(30);
        inactiveSection.setSchedule(List.of("W3"));
        inactiveSection = sectionRepository.save(inactiveSection);

        RegistrationDto registrationDto = new RegistrationDto(activeStudent.getId(), inactiveSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        // Verify
        List<RegistrationEntity> saved = registrationRepository.findByStudentId(activeStudent.getId());
        assertThat(saved).isEmpty();
    }

    @Test
    void whenCreateRegistrationWithoutCapacity_thenConflict() throws Exception {
        // Given
        availableSection.setMaxCapacity(0);
        sectionRepository.save(availableSection);

        RegistrationDto registrationDto = new RegistrationDto(activeStudent.getId(), availableSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        // Verify
        List<RegistrationEntity> saved = registrationRepository.findByStudentId(activeStudent.getId());
        assertThat(saved).isEmpty();
    }

    @Test
    void whenCreateRegistrationWithScheduleConflict_thenConflict() throws Exception {
        // Given
        RegistrationEntity existingRegistration = new RegistrationEntity();
        existingRegistration.setStudentId(activeStudent.getId());
        existingRegistration.setSectionId(availableSection.getId());
        existingRegistration.setAcademicTermId(activeTerm.getId());
        registrationRepository.save(existingRegistration);

        SectionEntity conflictingSection = new SectionEntity();
        conflictingSection.setTeacherId(4L);
        conflictingSection.setSubjectId(anotherAvailableSection.getSubjectId());
        conflictingSection.setAcademicTermId(activeTerm.getId());
        conflictingSection.setMaxCapacity(30);
        conflictingSection.setSchedule(List.of("L1", "J3")); // L1 CONFLICT
        conflictingSection = sectionRepository.save(conflictingSection);

        RegistrationDto conflictingRegistrationDto = new RegistrationDto(activeStudent.getId(), conflictingSection.getId());

        // When / Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingRegistrationDto)))
                .andExpect(status().isBadRequest());

        // Verify
        List<RegistrationEntity> saved = registrationRepository.findByStudentId(activeStudent.getId());
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getSectionId()).isEqualTo(availableSection.getId());
    }
}