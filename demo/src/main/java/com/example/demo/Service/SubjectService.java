package com.example.demo.Service;

import com.example.demo.DTO.SubjectDto;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Repository.AcademicHistorialRepository;
import com.example.demo.Repository.SubjectRepository;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final AcademicHistorialRepository academicHistorialRepository;

    public SubjectService(SubjectRepository subjectRepository, AcademicHistorialRepository academicHistorialRepository) {
        this.subjectRepository = subjectRepository;
        this.academicHistorialRepository = academicHistorialRepository;
    }

    public List<SubjectEntity> getSubjects() {return subjectRepository.findAll(); }

    public List<SubjectEntity> getPrerequisites(Long subjectId){
        SubjectEntity subject = getSubjectById(subjectId);

        if (subject.getPrerequisiteIds() == null || subject.getPrerequisiteIds().isEmpty()) {
            return Collections.emptyList();
        }

        return subjectRepository.findAllById(subject.getPrerequisiteIds());
    }

    public SubjectEntity getSubjectById(Long id){
        return subjectRepository.findById(id).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró una asignatura con el id: " + id
                ));
    }

    public SubjectEntity getSubjectByCode(String code) {
        return subjectRepository.findByCode(code).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró una asignatura con el codigo: " + code
                ));
    }

    public SubjectEntity getSubjectByName(String name) {
        return subjectRepository.findByName(name).
                orElseThrow(() -> new RuntimeException(
                        "No se encontró una asignatura con el nombre: " + name
                ));
    }

    public List<SubjectEntity> getSubjectsByStudyPlanId(Long studyPlanId){
        return subjectRepository.findByStudyPlanId(studyPlanId);
    }

    public SubjectEntity registerSubject(SubjectDto subjectDto) {
        if (subjectRepository.existsByCode(subjectDto.code())) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrada una asignatura con el código " + subjectDto.code()
            );
        }

        SubjectEntity subject = toEntity(subjectDto);
        validateSubject(subject);
        return subjectRepository.save(subject);
    }

    public SubjectEntity updateSubject(Long subjectId, SubjectDto subjectDto) {
        SubjectEntity currentSubject = getSubjectById(subjectId);

        if (subjectRepository.existsByCodeAndIdNot(subjectDto.code(), subjectId)) {
            throw new IllegalArgumentException(
                    "Ya se encuentra registrada una asignatura con el código " + subjectDto.code()
            );
        }

        SubjectEntity updatedSubject = toEntity(subjectDto);
        validateSubject(updatedSubject);

        currentSubject.setCode(updatedSubject.getCode());
        currentSubject.setName(updatedSubject.getName());
        currentSubject.setTEL(updatedSubject.getTEL());
        currentSubject.setSCT(updatedSubject.getSCT());
        currentSubject.setSemester(updatedSubject.getSemester());
        currentSubject.setPrerequisiteIds(updatedSubject.getPrerequisiteIds());
        currentSubject.setStudyPlanId(updatedSubject.getStudyPlanId());

        return subjectRepository.save(currentSubject);
    }

    private SubjectEntity toEntity(SubjectDto subjectDto) {
        SubjectEntity subject = new SubjectEntity();
        subject.setCode(subjectDto.code());
        subject.setName(subjectDto.name());
        subject.setTEL(subjectDto.TEL());
        subject.setSCT(subjectDto.SCT());
        subject.setSemester(subjectDto.semester());
        subject.setPrerequisiteIds(subjectDto.prerequisiteIds());
        subject.setStudyPlanId(subjectDto.studyPlanId());
        return subject;
    }

    private void validateSubject(SubjectEntity subject) {
        if (!(subject.getSCT() > 0 && subject.getSCT() <= 7)){
            throw new IllegalArgumentException(
                    "El SCT debe ser mayor a 0 o menor o igual a 7"
            );
        }
        String[] telNumbers = subject.getTEL().split("-");
        int suma = Integer.parseInt(telNumbers[0])
                + Integer.parseInt(telNumbers[1])
                + Integer.parseInt(telNumbers[2]);
        if (suma >= 8){
            throw new IllegalArgumentException(
                    "Las horas semanales no deben ser 8 o mayor"
            );
        }

        List<Long> prerequisitesIds = subject.getPrerequisiteIds();
        if (prerequisitesIds != null && !prerequisitesIds.isEmpty()) {
            if (prerequisitesIds.size() > 3) {
                throw new IllegalArgumentException("Los prerequisitos solo pueden ser 3 como máximo");
            }

            List<SubjectEntity> prerequisites = subjectRepository.findAllById(prerequisitesIds);

            for (SubjectEntity prereq : prerequisites) {
                if (!prereq.getStudyPlanId().equals(subject.getStudyPlanId())) {
                    throw new IllegalArgumentException(
                            "El prerequisito"+ prereq.getId() +"no pertenece al plan de estudio" + subject.getStudyPlanId()
                    );
                }

                // prerequisite be from a previous semester
                if (prereq.getSemester() >= subject.getSemester()) {
                    throw new IllegalArgumentException(
                            "El prerequisito"+ prereq.getId() +"debe pertenecer a un semestre estrictamente anterior"
                    );
                }
            }
        }
    }

    public void deleteSubject(Long subjectId){
        SubjectEntity subject = getSubjectById(subjectId);
        if (!academicHistorialRepository.existsBySubjectId(subjectId)){
            throw new IllegalArgumentException(
                    "Esta asignatura forma parte de un historial academico"
            );
        }
        subjectRepository.delete(subject);
    }
}
