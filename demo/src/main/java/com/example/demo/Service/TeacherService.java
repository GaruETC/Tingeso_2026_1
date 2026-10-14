package com.example.demo.Service;

import com.example.demo.DTO.TeacherDto;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Repository.SectionRepository;
import com.example.demo.Repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final AcademicTermService academicTermService;

    public TeacherService(TeacherRepository teacherRepository, SectionRepository sectionRepository, AcademicTermService academicTermService) {
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.academicTermService = academicTermService;
    }

    TeacherEntity getTeacherById(Long teacherId){
        return teacherRepository.findById(teacherId).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el id " + teacherId
                ));
    }

    TeacherEntity getTeacherByRun(String run){
        return teacherRepository.findByRun(run).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el run " + run
                ));
    }

    TeacherEntity getTeacherByFullName(String fullName){
        return teacherRepository.findByFullName(fullName).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el nombre completo " + fullName
                ));
    }

    List<TeacherEntity> getActiveTeachers(){
        return teacherRepository.findByStatus(TeacherStatus.ACTIVE);
    }

    TeacherEntity registerTeacher(TeacherDto teacherDto){
        // Normalize RUT and email
        String cleanRun = teacherDto.run().toUpperCase();
        String cleanEmail = teacherDto.email().toLowerCase();

        // Check that there is no existing teacher with the same RUT or email
        if (teacherRepository.existsByRun(cleanRun)){
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el run " + cleanRun
            );
        }
        if (teacherRepository.existsByEmail(cleanEmail)){
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el email " + cleanEmail
            );
        }

        TeacherEntity teacher = new TeacherEntity();
        teacher.setRun(cleanRun);
        teacher.setEmail(cleanEmail);
        teacher.setFullName(teacherDto.fullName());
        teacher.setTitle(teacherDto.title());
        teacher.setDegree(teacherDto.degree());
        teacher.setStatus(TeacherStatus.ACTIVE);

        return teacherRepository.save(teacher);
    }

    void deleteTeacher(TeacherEntity teacher){
        if (sectionRepository.existsByTeacherId(teacher.getId())){
            throw new IllegalArgumentException(
                    "No se puede eliminar el profesor " + teacher.getId() + " ya que tiene secciones asociadas"
            );
        }
        teacherRepository.delete(teacher);
    }

    TeacherEntity updateTeacher(Long teacherId, TeacherDto teacherDto){
        TeacherEntity currentTeacher = getTeacherById(teacherId);
        String cleanRun = teacherDto.run().toUpperCase();
        String cleanEmail = teacherDto.email().toLowerCase();

        if (teacherRepository.existsByRunAndIdNot(cleanRun, teacherId)) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el run " + cleanRun
            );
        }
        if (teacherRepository.existsByEmailAndIdNot(cleanEmail, teacherId)) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrado en el sistema el email " + cleanEmail
            );
        }

        boolean statusChangedtoInactive = currentTeacher.getStatus().equals(TeacherStatus.ACTIVE) &&
                                        teacherDto.status().equals(TeacherStatus.INACTIVE);

        if (statusChangedtoInactive){
            academicTermService.getActiveTerm().ifPresent(openTerm -> {
                if (sectionRepository.existsByTeacherIdAndAcademicTermId(teacherId, openTerm.getId())){
                    throw new IllegalStateException(
                            "El profesor tiene secciones asociadas en un periodo academico activo"
                    );
                }
            });
        }

        currentTeacher.setRun(cleanRun);
        currentTeacher.setEmail(cleanEmail);
        currentTeacher.setFullName(teacherDto.fullName());
        currentTeacher.setTitle(teacherDto.title());
        currentTeacher.setDegree(teacherDto.degree());
        currentTeacher.setStatus(teacherDto.status());

        return teacherRepository.save(currentTeacher);
    }
}
