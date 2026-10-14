package com.example.demo.Service;

import com.example.demo.Entity.*;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AcademicTermService {

    private final AcademicTermRepository academicTermRepository;
    private final RegistrationRepository registrationRepository;
    private final SectionRepository sectionRepository;
    private final AcademicHistorialRepository academicHistorialRepository;
    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AcademicTermService(AcademicTermRepository academicTermRepository, RegistrationRepository registrationRepository, SectionRepository sectionRepository, AcademicHistorialRepository academicHistorialRepository, StudentService studentService, StudentRepository studentRepository, SubjectRepository subjectRepository) {
        this.academicTermRepository = academicTermRepository;
        this.registrationRepository = registrationRepository;
        this.sectionRepository = sectionRepository;
        this.academicHistorialRepository = academicHistorialRepository;
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    public AcademicTermEntity getAcademicTermById(long id){
        return academicTermRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro el periodo academico con id: " + id
                ));
    }

    public AcademicTermEntity getAcademicTermByPeriod(String period){
        return academicTermRepository.findByPeriod(period).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró el periodo academico con periodo: " + period
                ));
    }

    public List<AcademicTermEntity> getInactiveTerms(){
        return academicTermRepository.findByStatus(AcademicTermStatus.CLOSED);
    }

    public Optional<AcademicTermEntity> getActiveTerm(){
        return academicTermRepository.findFirstByStatus(AcademicTermStatus.OPEN);
    }

    public void closeAcademicTerm(Long termId){
        AcademicTermEntity term = getAcademicTermById(termId);
        if (!term.getStatus().equals(AcademicTermStatus.OPEN)){
            throw new IllegalStateException("Solo se puede cerrar un periodo academico que no esta ABIERTO");
        }

        List<RegistrationEntity> registrations = registrationRepository.findByAcademicTermId(termId);
        List<Long> sectionsIdWithoutGrade = new ArrayList<>();
        for (RegistrationEntity reg : registrations){
            if (reg.getGrade() == null){
                sectionsIdWithoutGrade.add(reg.getSectionId());
            }
        }

        if (!sectionsIdWithoutGrade.isEmpty()){
            List<String> details = new ArrayList<>();
            for (Long sectionId : sectionsIdWithoutGrade){
                details.add(String.format("Seccion %d", sectionId));
            }
            throw new IllegalStateException(
                    "El cierre de periodo ha sido cancelado. Existen calificaciones faltantes en las siguientes secciones: "
                    + String.join(", ", details)
            );
        }

        for (RegistrationEntity reg : registrations){
            SectionEntity section = sectionRepository.findById(reg.getSectionId()).
                    orElseThrow(() -> new RuntimeException(
                            "No se encontro la seccion con id: " + reg.getSectionId()
                    ));

            AcademicHistorialEntity historial = new AcademicHistorialEntity();
            historial.setStudentId(reg.getStudentId());
            historial.setSubjectId(section.getSubjectId());
            historial.setAcademicTermId(termId);
            historial.setFinalGrade(reg.getGrade());

            boolean passed = reg.getGrade() >= 4.0f;
            if (passed){
                historial.setStatus(AcademicHistorialStatus.PASSED);
            } else{
                historial.setStatus(AcademicHistorialStatus.FAILED);
            }
            academicHistorialRepository.save(historial);

            StudentEntity student = studentService.getStudentById(reg.getStudentId());
            if (!passed){ // if has failed 2 times the same subject
                int failedCount = academicHistorialRepository
                        .countByStudentIdAndSubjectIdAndStatus(reg.getStudentId(), section.getSubjectId(), AcademicHistorialStatus.FAILED);
                if (failedCount >= 2){
                    student.setStatus(StudentStatus.DELETED);
                    studentRepository.save(student);
                }
            } else{ // if has passed all subjects in his studyplan
                int totalPlanSubjects = subjectRepository.countByStudyPlanId(reg.getStudentId());
                int totalApprovedSubjects = academicHistorialRepository
                        .countByStudentIdAndStatus(reg.getStudentId(), AcademicHistorialStatus.PASSED);
                if (totalApprovedSubjects == totalPlanSubjects){
                    student.setStatus(StudentStatus.GRADUATED);
                    studentRepository.save(student);
                }
            }
        }

        validateDeletedStudent();
        term.setStatus(AcademicTermStatus.CLOSED);
        academicTermRepository.save(term);
    }

    public void validateDeletedStudent(){

    }
}
