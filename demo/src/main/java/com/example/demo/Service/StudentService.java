package com.example.demo.Service;

import com.example.demo.Entity.StudentEntity;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Repository.AcademicHistorialRepository;
import com.example.demo.Repository.RegistrationRepository;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final AcademicHistorialRepository academicHistorialRepository;
    private final StudyPlanRepository studyPlanRepository;

    public StudentService(
            StudentRepository studentRepository,
            RegistrationRepository registrationRepository,
            AcademicHistorialRepository academicHistorialRepository,
            StudyPlanRepository studyPlanRepository) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.academicHistorialRepository = academicHistorialRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    public List<StudentEntity> getStudents(){
        return studentRepository.findAll();
    }

    public StudentEntity saveStudent(StudentEntity student){
        return studentRepository.save(student);
    }

    public StudentEntity getStudentById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el estudiante con id: " + id
                ));
    }

    public StudentEntity getStudentByRun(String run){
        return studentRepository.findByRun(run)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el estudiante con run: " + run
                ));
    }

    public StudentEntity updateStudent(StudentEntity student){
        StudentEntity currentStudent = getStudentById(student.getId());

        boolean careerChanged = !currentStudent.getCareerId().equals(student.getCareerId());
        boolean studyPlanChanged = !currentStudent.getStudyPlanId().equals(student.getStudyPlanId());

        if ((careerChanged || studyPlanChanged) && hasRecords(student.getId())) {
            throw new IllegalStateException(
                    "No se puede modificar la carrera o el plan de estudios "
                            + "porque el estudiante tiene inscripciones o historial académico"
            );
        }

        return studentRepository.save(student);
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

    public StudentEntity registerStudent(String run, String fullName, String email, Long careerId) {
        StudyPlanEntity studyPlan = studyPlanRepository
                .findByCareerIdAndStatus(careerId, StudyPlanStatus.VIGENTE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe un plan de estudio vigente para la carrera con id: "
                                + careerId
                ));

        StudentEntity student = new StudentEntity();
        student.setRun(run);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setCareerId(careerId);
        student.setStudyPlanId(studyPlan.getId());

        return saveStudent(student);
    }


}
