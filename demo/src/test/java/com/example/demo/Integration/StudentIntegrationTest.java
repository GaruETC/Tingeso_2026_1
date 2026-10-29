package com.example.demo.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.DTO.StudentDto;
import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Repository.CareerRepository;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import com.example.demo.Service.KeycloakService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"ADMIN"})
class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private KeycloakService keycloakService;

    private CareerEntity activeCareer;
    private StudyPlanEntity activePlan;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        studyPlanRepository.deleteAll();
        careerRepository.deleteAll();

        activeCareer = new CareerEntity();
        activeCareer.setCode("ING-COMP");
        activeCareer.setName("Ingenieria en Computacion");
        activeCareer.setDescription("Carrera activa");
        activeCareer.setStatus(CareerStatus.ACTIVO);
        activeCareer = careerRepository.save(activeCareer);

        activePlan = new StudyPlanEntity();
        activePlan.setCareerId(activeCareer.getId());
        activePlan.setCode("PLAN-2026");
        activePlan.setStatus(StudyPlanStatus.VIGENTE);
        activePlan = studyPlanRepository.save(activePlan);

        when(keycloakService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("mocked-keycloak-id");
    }

    // 3 SUCCESS

    @Test
    void whenRegisterStudent_thenStudentIsStoredInDatabase() throws Exception {
        // Given
        StudentDto studentDto = new StudentDto(
                "11111111-1", "juan.perez@usach.cl", "Juan Perez", activeCareer.getId(), activePlan.getId()
        );
        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.run").value("11111111-1"))
                .andExpect(jsonPath("$.email").value("juan.perez@usach.cl"))
                .andExpect(jsonPath("$.fullName").value("Juan Perez"));
        // Verify
        StudentEntity saved = studentRepository.findByRun("11111111-1").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(StudentStatus.REGULAR);
    }

    @Test
    void whenRegisterStudentWithDifferentCase_thenDataIsNormalized() throws Exception {

        // Given
        StudentDto studentDto = new StudentDto(
                "22222222-k", "MARIA.LOPEZ@USACH.CL", "Maria Lopez", activeCareer.getId(), activePlan.getId()
        );

        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.run").value("22222222-K"))
                .andExpect(jsonPath("$.email").value("maria.lopez@usach.cl"));

        // Verify
        StudentEntity saved = studentRepository.findByRun("22222222-K").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("maria.lopez@usach.cl");
    }

    @Test
    void whenRegisterMultipleStudents_thenAllAreStored() throws Exception {

        // Given
        StudentDto student1 = new StudentDto(
                "11111111-1", "juan.perez@usach.cl", "Juan Perez", activeCareer.getId(), activePlan.getId()
        );
        StudentDto student2 = new StudentDto(
                "33333333-3", "ana.silva@usach.cl", "Ana Silva", activeCareer.getId(), activePlan.getId()
        );

        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student2)))
                .andExpect(status().isCreated());

        // Verify
        assertThat(studentRepository.findAll()).hasSize(2);
    }

    // 3 FAILED

    @Test
    void whenRegisterStudentWithExistingRun_thenBadRequest() throws Exception {

        // Given
        StudentEntity existingStudent = new StudentEntity();
        existingStudent.setRun("12345678-9");
        existingStudent.setEmail("primero@usach.cl");
        existingStudent.setFullName("Primero");
        existingStudent.setCareerId(activeCareer.getId());
        existingStudent.setStudyPlanId(activePlan.getId());
        existingStudent.setStatus(StudentStatus.REGULAR);
        studentRepository.save(existingStudent);

        StudentDto duplicateRunStudent = new StudentDto(
                "12345678-9", "segundo@usach.cl", "Segundo", activeCareer.getId(), activePlan.getId()
        );

        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRunStudent)))
                .andExpect(status().isBadRequest());

        // Verify
        assertThat(studentRepository.findAll()).hasSize(1);
    }

    @Test
    void whenRegisterStudentWithExistingEmail_thenBadRequest() throws Exception {

        // Given
        StudentEntity existingStudent = new StudentEntity();
        existingStudent.setRun("11111111-1");
        existingStudent.setEmail("mismo.correo@usach.cl");
        existingStudent.setFullName("Primero");
        existingStudent.setCareerId(activeCareer.getId());
        existingStudent.setStudyPlanId(activePlan.getId());
        existingStudent.setStatus(StudentStatus.REGULAR);
        studentRepository.save(existingStudent);

        StudentDto duplicateEmailStudent = new StudentDto(
                "22222222-2", "mismo.correo@usach.cl", "Segundo", activeCareer.getId(), activePlan.getId()
        );

        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailStudent)))
                .andExpect(status().isBadRequest());

        // Verify
        assertThat(studentRepository.findAll()).hasSize(1);
    }

    @Test
    void whenRegisterStudentWithInactiveCareer_thenBadRequest() throws Exception {

        // Given
        CareerEntity inactiveCareer = new CareerEntity();
        inactiveCareer.setCode("ING-INACTIVA");
        inactiveCareer.setName("Carrera Inactiva");
        inactiveCareer.setDescription("Cerrada");
        inactiveCareer.setStatus(CareerStatus.INACTIVO);
        inactiveCareer = careerRepository.save(inactiveCareer);

        StudentDto studentDto = new StudentDto(
                "99999999-9", "error@usach.cl", "Error", inactiveCareer.getId(), activePlan.getId()
        );

        // When / Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isBadRequest());

        // Verify
        assertThat(studentRepository.findAll()).isEmpty();
    }
}