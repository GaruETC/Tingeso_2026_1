package com.example.demo.Service;

import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.DTO.StudentDto;
import com.example.demo.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final AcademicHistorialRepository academicHistorialRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final CareerRepository careerRepository;
    private final KeycloakService keycloakService;

    public StudentService(
            StudentRepository studentRepository,
            RegistrationRepository registrationRepository,
            AcademicHistorialRepository academicHistorialRepository,
            StudyPlanRepository studyPlanRepository, CareerRepository careerRepository, KeycloakService keycloakService) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.academicHistorialRepository = academicHistorialRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.careerRepository = careerRepository;
        this.keycloakService = keycloakService;
    }

    public List<StudentEntity> getStudents(){
        return studentRepository.findAll();
    }

    public List<StudentEntity> getStudentsByStatus(StudentStatus status){
        return studentRepository.findByStatus(status);
    }

    public StudentEntity getStudentById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el estudiante con id: " + id
                ));
    }

    public StudentEntity getStudentByEmail(String email){
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el estudiante con email: " + email
                ));
    }

    public StudentEntity getStudentByRun(String run){
        return studentRepository.findByRun(run)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el estudiante con run: " + run
                ));
    }

    public StudentEntity updateStudent(Long studentId, StudentDto studentDto){
        StudentEntity currentStudent = getStudentById(studentId);
        String cleanRun = studentDto.run().toUpperCase();
        String cleanEmail = studentDto.email().toLowerCase();

        if (studentRepository.existsByRunAndIdNot(cleanRun, studentId)) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el run " + cleanRun
            );
        }
        if (studentRepository.existsByEmailAndIdNot(cleanEmail, studentId)) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el email " + cleanEmail
            );
        }

        boolean careerChanged = !currentStudent.getCareerId().equals(studentDto.careerId());
        boolean studyPlanChanged = !currentStudent.getStudyPlanId().equals(studentDto.studyPlanId());

        if ((careerChanged || studyPlanChanged) && hasRecords(studentId)) {
            throw new IllegalStateException(
                    "No se puede modificar la carrera o el plan de estudios "
                            + "porque el estudiante tiene inscripciones o historial académico"
            );
        }

        currentStudent.setRun(cleanRun);
        currentStudent.setEmail(cleanEmail);
        currentStudent.setFullName(studentDto.fullName());
        currentStudent.setCareerId(studentDto.careerId());
        currentStudent.setStudyPlanId(studentDto.studyPlanId());

        return studentRepository.save(currentStudent);
    }

    public boolean hasRecords(Long studentId){
        return registrationRepository.existsByStudentId(studentId) ||
                academicHistorialRepository.existsByStudentId(studentId);
    }

    public void deleteStudent(Long id){
        StudentEntity student = getStudentById(id);
        if (hasRecords(id)){
            throw new IllegalStateException(
                    "No se puede eliminar el estudiante ya que tiene historial academico o inscripciones"
            );
        }
        studentRepository.delete(student);
    }

    public StudentEntity registerStudent(StudentDto studentDto) {
        // Normalize RUT and email
        String cleanRun = studentDto.run().toUpperCase();
        String cleanEmail = studentDto.email().toLowerCase();

        // Check that there is no existing teacher with the same RUT or email
        if (studentRepository.existsByRun(cleanRun)){
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el run " + cleanRun
            );
        }
        if (studentRepository.existsByEmail(cleanEmail)){
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el email " + cleanEmail
            );
        }

        CareerEntity career = careerRepository.findById(studentDto.careerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la carrera con id: " + studentDto.careerId()
                ));
        if (career.getStatus().equals(CareerStatus.INACTIVO)){
            throw new IllegalStateException(
                    "No se puede registrar en la carrera ya que esta inactiva"
            );
        }

        StudyPlanEntity studyPlan = studyPlanRepository
                .findByCareerIdAndStatus(studentDto.careerId(), StudyPlanStatus.VIGENTE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe un plan de estudio vigente para la carrera con id: " + studentDto.careerId()
                ));

        String tempPassword = "estudiante123";
        String keycloakUserId = keycloakService.createUserWithRole(
                studentDto.fullName().split(" ")[0],
                cleanEmail,
                studentDto.fullName(),
                tempPassword,
                "STUDENT"
        );

        try {
            StudentEntity student = new StudentEntity();
            student.setRun(cleanRun);
            student.setEmail(cleanEmail);
            student.setFullName(studentDto.fullName());
            student.setCareerId(studentDto.careerId());
            student.setStudyPlanId(studyPlan.getId());
            student.setMatriculation(false);
            student.setStatus(StudentStatus.REGULAR);

            return studentRepository.save(student);
        } catch (Exception e) {
            keycloakService.deleteUser(keycloakUserId);
            throw new RuntimeException("Error al registrar estudiante en la base de datos");
        }
    }

    public void updateStudentStatus(Long id, StudentStatus status){
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Estudiante no encontrado: " + id
                ));
        boolean canUpdate = status == StudentStatus.REGULAR || status == StudentStatus.APLAZADO || status == StudentStatus.RETIRO_TEMPORAL;
        if (!canUpdate){
            throw new IllegalStateException(
                    "Solo se puede modificar REGULAR, APLAZADO y RETIRO_TEMPORAL"
            );
        }

        student.setStatus(status);
        studentRepository.save(student);
    }

    @Transactional
    public StudentEntity matricularEstudiante(Long id) {
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        student.setMatriculation(true);
        return studentRepository.save(student);
    }
}
