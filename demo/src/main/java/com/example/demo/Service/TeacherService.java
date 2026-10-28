package com.example.demo.Service;

import com.example.demo.DTO.TeacherDto;
import com.example.demo.Entity.Enum.AcademicTermStatus;
import com.example.demo.Entity.Enum.TeacherStatus;
import com.example.demo.Entity.TeacherEntity;
import com.example.demo.Repository.AcademicTermRepository;
import com.example.demo.Repository.SectionRepository;
import com.example.demo.Repository.TeacherRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final AcademicTermService academicTermService;
    private final KeycloakService keycloakService;

    public TeacherService(TeacherRepository teacherRepository, SectionRepository sectionRepository, AcademicTermService academicTermService, KeycloakService keycloakService) {
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.academicTermService = academicTermService;
        this.keycloakService = keycloakService;
    }

    public List<TeacherEntity> getTeachers(){
        return teacherRepository.findAll();
    }

    public List<TeacherEntity> getTeachersByStatus(TeacherStatus status){
        return teacherRepository.findByStatus(status);
    }

    public TeacherEntity getTeacherById(Long teacherId){
        return teacherRepository.findById(teacherId).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el id " + teacherId
                ));
    }

    public TeacherEntity getTeacherByRun(String run){
        return teacherRepository.findByRun(run).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el run " + run
                ));
    }

    public TeacherEntity getTeacherByFullName(String fullName){
        return teacherRepository.findByFullName(fullName).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró un profesor con el nombre completo " + fullName
                ));
    }

    public List<TeacherEntity> getActiveTeachers(){
        return teacherRepository.findByStatus(TeacherStatus.ACTIVO);
    }

    public TeacherEntity registerTeacher(TeacherDto teacherDto){
        // Normalize RUT and email
        String cleanRun = teacherDto.run().toUpperCase();
        String cleanEmail = teacherDto.email().toLowerCase();
        String cleanFullName = teacherDto.fullName().trim().replaceAll("\\s+", " ");

        if (cleanFullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del docente es obligatorio");
        }

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

        String tempPassword = "docente123";
        String keycloakUserId = keycloakService.createUserWithRole(
                cleanFullName.split(" ")[0],
                cleanEmail,
                cleanFullName,
                tempPassword,
                "TEACHER"
        );

        try {
            TeacherEntity teacher = new TeacherEntity();
            teacher.setRun(cleanRun);
            teacher.setEmail(cleanEmail);
            teacher.setFullName(cleanFullName);
            teacher.setTitle(teacherDto.title());
            teacher.setDegree(teacherDto.degree());
            teacher.setStatus(TeacherStatus.ACTIVO);

            return teacherRepository.save(teacher);
        } catch (Exception e) {
            keycloakService.deleteUser(keycloakUserId);
            throw new RuntimeException("Error al registrar docente en la base de datos");
        }
    }

    @Transactional
    public void deleteTeacher(Long id){
        TeacherEntity teacher = getTeacherById(id);
        if (sectionRepository.existsByTeacherId(id)){
            throw new IllegalArgumentException(
                    "No se puede eliminar el profesor " + teacher.getId() + " ya que tiene secciones asociadas"
            );
        }
        keycloakService.deleteUserByUsername(teacher.getFullName().split(" ")[0]);

        teacherRepository.delete(teacher);
    }

    public TeacherEntity updateTeacher(Long teacherId, TeacherDto teacherDto){
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

        currentTeacher.setRun(cleanRun);
        currentTeacher.setEmail(cleanEmail);
        currentTeacher.setFullName(teacherDto.fullName());
        currentTeacher.setTitle(teacherDto.title());
        currentTeacher.setDegree(teacherDto.degree());

        return teacherRepository.save(currentTeacher);
    }

    public void updateTeacherStatus(Long teacherId, TeacherStatus status){
        TeacherEntity currentTeacher = getTeacherById(teacherId);

        if (currentTeacher.getStatus() == status) {
            return;
        }

        if (status == TeacherStatus.INACTIVO) {
            academicTermService.getActiveTerm().ifPresent(openTerm -> {
                if (sectionRepository.existsByTeacherIdAndAcademicTermId(teacherId, openTerm.getId())) {
                    throw new IllegalStateException(
                            "No se puede cambiar el estado a INACTIVO mientras el docente tenga secciones asignadas en un período académico ABIERTO"
                    );
                }
            });
        }
        currentTeacher.setStatus(status);
        teacherRepository.save(currentTeacher);
    }
}
