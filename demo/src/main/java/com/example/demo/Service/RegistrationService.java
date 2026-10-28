package com.example.demo.Service;

import com.example.demo.DTO.RegistrationDto;
import com.example.demo.DTO.SubmitGradesDto;
import com.example.demo.Entity.*;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.StudentStatus;
import com.example.demo.Repository.*;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final StudentService studentService;
    private final SectionService sectionService;
    private final SubjectService subjectService;
    private final AcademicTermService academicTermService;
    private final AcademicHistorialRepository academicHistorialRepository;
    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;

    public RegistrationService(RegistrationRepository registrationRepository, StudentService studentService, SectionService sectionService, SubjectService subjectService, AcademicTermService academicTermService, AcademicHistorialRepository academicHistorialRepository, SubjectRepository subjectRepository, SectionRepository sectionRepository) {
        this.registrationRepository = registrationRepository;
        this.studentService = studentService;
        this.sectionService = sectionService;
        this.subjectService = subjectService;
        this.academicTermService = academicTermService;
        this.academicHistorialRepository = academicHistorialRepository;
        this.subjectRepository = subjectRepository;
        this.sectionRepository = sectionRepository;
    }

    public List<RegistrationEntity> getRegistrations(){ return registrationRepository.findAll(); }

    public List<RegistrationEntity> getRegistrationsByStudentIdAndTermId(Long studentId, Long termId){
        return registrationRepository.findByStudentIdAndAcademicTermId(studentId, termId);
    }

    public RegistrationEntity getRegistrationById(Long id){
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro la inscripcion con id: " + id
                ));
    }

    public List<RegistrationEntity> getRegistrationByStudentId(Long studentId){
        return registrationRepository.findByStudentId(studentId);
    }

    public List<RegistrationEntity> getRegistrationsBySectionId(Long sectionId){
        return registrationRepository.findBySectionId(sectionId);
    }

    public List<RegistrationEntity> getRegistrationByAcademicTermId(Long termId){
        return registrationRepository.findByAcademicTermId(termId);
    }

    public RegistrationEntity createRegistration(RegistrationDto registrationDto){
        StudentEntity student = studentService.getStudentById(registrationDto.studentId());
        SectionEntity section = sectionService.getSectionById(registrationDto.sectionId());
        SubjectEntity subject = subjectService.getSubjectById(section.getSubjectId());
        AcademicTermEntity activeTerm = academicTermService.getActiveTerm()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay periodo academico activo en este momento"
                ));
        // student regular and with matriculation
        if (student.getStatus() != StudentStatus.REGULAR){
            throw new IllegalStateException(
                    "Para inscribirse a una seccion, el estudiante debe estar en estado REGULAR"
            );
        }
        if (!student.isMatriculation()){
            throw new IllegalStateException(
                    "Para inscribirse a una seccion, el estudiante debe estar matriculado"
            );
        }

        // Section in active academic term
        if (!section.getAcademicTermId().equals(activeTerm.getId())){
            throw new IllegalStateException(
                    "La seccion seleccionada no es parte del periodo academico activo"
            );
        }
        // subject in student's studyplan
        if (!subject.getStudyPlanId().equals(student.getStudyPlanId())){
            throw new IllegalStateException(
                   "La asignatura no pertenece al plan de estudios del estudiante: " + student.getId()
            );
        }
        if (registrationRepository.existsByStudentIdAndSectionId(student.getId(), section.getId())){
            throw new IllegalStateException(
                    "Ya existe una inscripcion del estudiante con la seccion: " + section.getId()
            );
        }
        long occupiedSeats = registrationRepository.countBySectionId(section.getId());
        if (occupiedSeats >= section.getMaxCapacity()){
            throw new IllegalStateException(
                    "La seccion no tiene cupos disponibles"
            );
        }

        validateSubjectApprovals(student.getId(), subject.getId());
        validateApprovedPrereq(student.getId(), subject.getId());
        validateNoLowerSemester(student, subject, activeTerm.getId());
        validateStudentSchedule(student.getId(), activeTerm.getId(), section.getSchedule());

        section.setMaxCapacity(section.getMaxCapacity() - 1);

        RegistrationEntity registration = new RegistrationEntity();
        registration.setStudentId(student.getId());
        registration.setSectionId(section.getId());
        registration.setRegistrationDate(LocalDate.now());
        registration.setAcademicTermId(section.getAcademicTermId());

        return registrationRepository.save(registration);
    }

    // Validate if you can register in a section by his approval status
    public void validateSubjectApprovals(Long studentId, Long subjectId){
        List<AcademicHistorialEntity> historyList = academicHistorialRepository
                .findByStudentIdAndSubjectId(studentId, subjectId);

        //if subejct is already approve
        for (AcademicHistorialEntity record : historyList){
            if (record.getStatus().equals(AcademicHistorialStatus.APROBADO)){
                throw new IllegalStateException(
                        "La asignatura ya fue aprovada"
                );
            }
        }
        //count how many times failed
        int failCount = 0;
        for (AcademicHistorialEntity record : historyList){
            if (record.getStatus().equals(AcademicHistorialStatus.REPROBADO)){
                failCount++;
            }
        }
        // dont let do the registration if already failed 2 times
        if (failCount >= 2){
            throw new IllegalStateException(
                    "No puede inscribirse ya que la asignatura ya ha sido reprobada al menos 2 veces"
            );
        }
    }

    // Validate that prerequisites were approved
    public boolean validateApprovedPrereq(Long studentId, Long subjectId){
        SubjectEntity subject = subjectService.getSubjectById(subjectId);
        List<Long> prerequeIds = subject.getPrerequisiteIds();

        if (prerequeIds == null || prerequeIds.isEmpty()){
            return true;
        }

        for (Long prereqId : prerequeIds){
            if (!academicHistorialRepository
                    .existsByStudentIdAndSubjectIdAndStatus(studentId, prereqId, AcademicHistorialStatus.APROBADO)){
                return false;
            }
        }
        return true;
    }

    public void validateNoLowerSemester(StudentEntity student, SubjectEntity subject, Long activeTermId){
        int subjectSemester = subject.getSemester();
        List<SubjectEntity> lowerSubjects = subjectRepository
                .findByStudyPlanIdAndSemesterLessThan(student.getStudyPlanId(), subjectSemester);

        for (SubjectEntity lowerSubject : lowerSubjects){
            //already approved?
            boolean passed = academicHistorialRepository
                    .existsByStudentIdAndSubjectIdAndStatus(student.getId(), lowerSubject.getId(), AcademicHistorialStatus.APROBADO);
            if (passed){
                continue;
            }
            //exist section?
            Optional<SectionEntity> sectionOpt = sectionRepository
                    .findBySubjectIdAndAcademicTermId(lowerSubject.getId(), activeTermId);
            if (sectionOpt.isEmpty()){
                continue;
            }
            SectionEntity lowerSection = sectionOpt.get();
            //already registered?
            boolean alreadyRegistered = registrationRepository
                    .existsByStudentIdAndSectionId(student.getId(), lowerSection.getId());
            if (alreadyRegistered){
                continue;
            }
            // approved prerequisites?
            if (!validateApprovedPrereq(student.getId(), lowerSubject.getId())){
                continue;
            }
            throw new IllegalStateException(
                    "No puedes inscribir el ramo mientras tengas ramos pendientes de semestres inferiores disponibles para inscripción"
            );
        }
    }

    public void validateStudentSchedule(Long studentId, Long termId, List<String> schedule){
        if (schedule == null || schedule.isEmpty()) {
            return;
        }

        List<RegistrationEntity> currentRegistrations = registrationRepository
                .findByStudentIdAndAcademicTermId(studentId, termId);

        for (RegistrationEntity reg : currentRegistrations) {
            SectionEntity existingSection = sectionService.getSectionById(reg.getSectionId());
            if (existingSection.getSchedule() != null) {
                for (String occupiedModule : existingSection.getSchedule()) {
                    if (schedule.contains(occupiedModule)) {
                        throw new IllegalStateException(
                                "Conflicto de horario: ya tienes clases inscritas en el módulo: " + occupiedModule
                        );
                    }
                }
            }
        }
    }

    public void cancelRegistration(Long id){
        RegistrationEntity registration = getRegistrationById(id);
        AcademicTermEntity activeTerm = academicTermService.getActiveTerm()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay periodo academico activo en este momento"
                ));

        SectionEntity section = sectionService.getSectionById(registration.getSectionId());
        section.setMaxCapacity(section.getMaxCapacity() + 1);

        registrationRepository.delete(registration);
    }

    public void submitGrades(Long sectionId, SubmitGradesDto dto){
        SectionEntity section = sectionService.getSectionById(sectionId);

        AcademicTermEntity term = academicTermService.getAcademicTermById(section.getAcademicTermId());
        if (term.getStatus().equals(AcademicTermStatus.CERRADO)){
            throw new IllegalStateException("No se puede registrar notas ya que el periodo academico al que corresponde la seccion, esta cerrado");
        }

        if (!section.getTeacherId().equals(dto.teacherId())){
            throw new IllegalStateException("Solo el profesor de la seccion puede colocar notas");
        }

        for (SubmitGradesDto.StudentGradeDto gradeDto : dto.grades()){
            RegistrationEntity registration = getRegistrationById(gradeDto.registrationId());

            if (!registration.getSectionId().equals(section.getId())){
                throw new IllegalStateException("La inscripcion no corresponde a la seccion dada");
            }

            if (gradeDto.grade() == null || gradeDto.grade() < 1.0f || gradeDto.grade() > 7.0f) {
                throw new IllegalArgumentException("La nota debe estar entre 1.0 y 7.0");
            }

            registration.setGrade(gradeDto.grade());
            registrationRepository.save(registration);
        }
    }
}
