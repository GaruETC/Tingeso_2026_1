package com.example.demo.Service;

import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Entity.SubjectEntity;
import com.example.demo.Repository.AcademicHistorialRepository;
import com.example.demo.Repository.SubjectRepository;
import org.springframework.stereotype.Service;

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

    public SubjectEntity saveSubject(SubjectEntity subject) {
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
                            String.format("El prerequisito '%s' (ID: %d) no pertenece al plan de estudio %d",
                                    prereq.getName(), prereq.getId(), subject.getStudyPlanId())
                    );
                }

                // que el prerequisito sea de semestre anterior
                if (prereq.getSemester() >= subject.getSemester()) {
                    throw new IllegalArgumentException(
                            String.format("El prerequisito '%s' (Semestre %d) debe pertenecer a un semestre estrictamente anterior",
                                    prereq.getName(), prereq.getSemester())
                    );
                }
            }
        }
        return subjectRepository.save(subject);
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
