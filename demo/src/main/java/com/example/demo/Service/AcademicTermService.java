package com.example.demo.Service;

import com.example.demo.DTO.AcademicTermDto;
import com.example.demo.Entity.*;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<AcademicTermEntity> getAcademicTerms(){ return academicTermRepository.findAll(); }

    public List<AcademicTermEntity> getTermsByStatus(AcademicTermStatus status){
        return academicTermRepository.findByStatus(status);
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
        return academicTermRepository.findByStatus(AcademicTermStatus.CERRADO);
    }

    public Optional<AcademicTermEntity> getActiveTerm(){
        return academicTermRepository.findFirstByStatus(AcademicTermStatus.ABIERTO);
    }

    public AcademicTermEntity createAcademicTerm(AcademicTermDto dto){
        String period = dto.year() + "-" + dto.semester();

        if (academicTermRepository.existsByPeriod(period)){
            throw new IllegalArgumentException("Ya existe un periodo academico con periodo: " + period);
        }
        if (academicTermRepository.existsByStatus(AcademicTermStatus.ABIERTO)){
            throw new IllegalStateException(
                    "Ya existe un periodo academico con estado ABIERTO"
            );
        }
        AcademicTermEntity term = new AcademicTermEntity();
        term.setPeriod(period);
        term.setStatus(AcademicTermStatus.ABIERTO);

        return academicTermRepository.save(term);
    }

    @Transactional
    public void closeAcademicTerm(Long termId){
        AcademicTermEntity term = getAcademicTermById(termId);
        if (!term.getStatus().equals(AcademicTermStatus.ABIERTO)){
            throw new IllegalStateException("Solo se puede cerrar un periodo academico que este ABIERTO");
        }

        List<RegistrationEntity> registrations = registrationRepository.findByAcademicTermId(termId);
        Set<Long> sectionsIdWithoutGrade = new HashSet<>();
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
                historial.setStatus(AcademicHistorialStatus.APROBADO);
            } else{
                historial.setStatus(AcademicHistorialStatus.REPROBADO);
            }
            academicHistorialRepository.save(historial);

            StudentEntity student = studentService.getStudentById(reg.getStudentId());
            if (!passed){ // if student has failed 2 times the same subject
                int failedCount = academicHistorialRepository
                        .countByStudentIdAndSubjectIdAndStatus(reg.getStudentId(), section.getSubjectId(), AcademicHistorialStatus.REPROBADO);
                if (failedCount >= 2){
                    student.setStatus(StudentStatus.ELIMINADO);
                    studentRepository.save(student);
                }
            } else{ // if student has passed all subjects in his studyplan
                int totalPlanSubjects = subjectRepository.countByStudyPlanId(student.getStudyPlanId());
                int totalApprovedSubjects = academicHistorialRepository
                        .countByStudentIdAndStatus(reg.getStudentId(), AcademicHistorialStatus.APROBADO);
                if (totalApprovedSubjects == totalPlanSubjects){
                    student.setStatus(StudentStatus.EGRESADO);
                    studentRepository.save(student);
                }
            }
        }
        term.setStatus(AcademicTermStatus.CERRADO);
        academicTermRepository.save(term);

        studentRepository.resetAllMatriculations();
    }
}
