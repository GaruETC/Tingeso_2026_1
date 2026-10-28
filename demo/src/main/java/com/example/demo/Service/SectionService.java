package com.example.demo.Service;

import com.example.demo.DTO.SectionDto;
import com.example.demo.Entity.AcademicTermEntity;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.SectionEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Repository.RegistrationRepository;
import com.example.demo.Repository.SectionRepository;
import com.example.demo.Repository.TeacherRepository;
import org.apache.tomcat.util.http.fileupload.ThresholdingOutputStream;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final TeacherService teacherService;
    private final SubjectService subjectService;
    private final AcademicTermService academicTermService;
    private final RegistrationRepository registrationRepository;

    public SectionService(SectionRepository sectionRepository, TeacherService teacherService, SubjectService subjectService, AcademicTermService academicTermService, RegistrationRepository registrationRepository) {
        this.sectionRepository = sectionRepository;
        this.teacherService = teacherService;
        this.subjectService = subjectService;
        this.academicTermService = academicTermService;
        this.registrationRepository = registrationRepository;
    }

    public List<SectionEntity> getSections(){
        return sectionRepository.findAll();
    }

    public List<SectionEntity> getSectionsByTeacherIdAndAcademicTermId(Long teacherId, Long termId){
        return sectionRepository.findByTeacherIdAndAcademicTermId(teacherId, teacherId);
    }

    public SectionEntity getSectionById(Long id){
        return sectionRepository.findById(id).
                orElseThrow(() -> new RuntimeException(
                        "No se encontro la seccion con id: " + id
                ));
    }

    public List<SectionEntity> getSectionsByAcademicTermId(Long id){
        return sectionRepository.findByAcademicTermId(id);
    }

    public List<SectionEntity> getSectionsByTeacherId(Long id){
        return sectionRepository.findByTeacherId(id);
    }

    public SectionEntity getSectionBySubjectId(Long id){
        return sectionRepository.findBySubjectId(id).
                orElseThrow(() ->  new RuntimeException(
                        "No se encontro una seccion con la asignatura: " + id
                ));
    }

    public SectionEntity createSection(SectionDto sectionDTO){
        TeacherEntity teacher = teacherService.getTeacherById(sectionDTO.teacherId());
        SubjectEntity subject = subjectService.getSubjectById(sectionDTO.subjectId());
        AcademicTermEntity academicTerm = academicTermService.getAcademicTermById(sectionDTO.academicTermId());

        if (teacher.getStatus() != TeacherStatus.ACTIVO){
            throw new IllegalStateException(
                    "El profesor asociado debe encontrarse activo"
            );
        }
        if (sectionRepository.existsBySubjectIdAndAcademicTermId(subject.getId(), sectionDTO.academicTermId())){
            throw new IllegalStateException(
                    "Ya existe una seccion asociada a esta asignatura en este periodo academico"
            );
        }
        if (academicTerm.getStatus() == AcademicTermStatus.CERRADO){
            throw new IllegalStateException(
                    "Este periodo academico esta cerrado"
            );
        }
        int maxCapacity = sectionDTO.maxCapacity();
        if (maxCapacity <= 0 || maxCapacity > 60){
            throw new IllegalStateException(
                    "La capacidad maxima debe ser de 1 a 60 estudiantes"
            );
        }

        int modulesHours = validateSchedule(sectionDTO.schedule()) * 2;
        String[] telChar = subject.getTEL().split("-");
        int sumaTel = 0;
        for (String num : telChar){
            sumaTel += Integer.parseInt(num);
        }
        if (modulesHours != sumaTel){
            throw new IllegalArgumentException("" +
                    "Las horas de modulos de la seccion con coinciden con el TEL");
        }

        validateTeacherSchedule(teacher.getId(), academicTerm.getId(), sectionDTO.schedule());

        SectionEntity section = new SectionEntity();
        section.setTeacherId(teacher.getId());
        section.setSubjectId(subject.getId());
        section.setAcademicTermId(academicTerm.getId());
        section.setSchedule(sectionDTO.schedule());
        section.setMaxCapacity(maxCapacity);

        return sectionRepository.save(section);
    }

    public void validateTeacherSchedule(Long teacherId, Long academicTermId, List<String> newSchedule){
        List<SectionEntity> teachersSections = sectionRepository
                .findByTeacherIdAndAcademicTermId(teacherId, academicTermId);

        for (SectionEntity section : teachersSections){
            if (!section.getSchedule().isEmpty()){
                for (String module : section.getSchedule()){
                    if (newSchedule.contains(module)){
                        throw new IllegalStateException(
                                "Hay un conflicto de horarios con la seccion que se trata de crear"
                        );
                    }
                }
            }
        }
    }

    public int validateSchedule(List<String> schedule){
        if (schedule.isEmpty()){
            throw new IllegalStateException(
                    "La seccion debe tener al menos un bloque"
            );
        }
        int modules = 0;
        Set<String> record = new HashSet<>();
        for (String module : schedule) {
            String[] parts = module.split("");
            if (!module.matches("^[LMWJV][1-6]$")) {
                throw new IllegalStateException(
                        "Al menos uno de los modulos tiene el formato incorrecto"
                );
            }
            if (!record.add(module)) {
                throw new IllegalArgumentException(
                        "La seccion contiene modulos duplicados"
                );
            }
            modules++;
        }
        return modules;
    }

    public void deleteSection(Long id){
        SectionEntity section = getSectionById(id);

        if (registrationRepository.existsBySectionId(id)){
            throw new IllegalStateException(
                    "No se puede eliminar la seccion ya que tiene al menos un estudiante inscrito"
            );
        }
        sectionRepository.delete(section);
    }
}
