package com.example.demo.Service;

import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudentRepository studentRepository;

    public StudyPlanService(StudyPlanRepository studyPlanRepository, StudentRepository studentRepository) {
        this.studyPlanRepository = studyPlanRepository;
        this.studentRepository = studentRepository;
    }

    public StudyPlanEntity getStudyPlanById(Long studyPlanId){
        return studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new RuntimeException(
                "No se encontró el plan de estudio con id: " + studyPlanId
        ));

    }

    public StudyPlanEntity saveStudyPlan(StudyPlanEntity studyPlan){
        return studyPlanRepository.save(studyPlan);
    }

    public StudyPlanEntity createStudyPlan(Long careerId){
        StudyPlanEntity currentStudyPlan = studyPlanRepository
                .findByCareerIdAndStatus(careerId, StudyPlanStatus.VIGENTE)
                .orElseThrow(() -> new RuntimeException(
                        "No se econtró un plan de estudio vigente para la carrera con id: " + careerId
                ));
        currentStudyPlan.setStatus(StudyPlanStatus.NO_VIGENTE);

        StudyPlanEntity studyPlan = new StudyPlanEntity();
        studyPlan.setCareerId(careerId);
        return studyPlan;
    }

    public void deleteStudyPlan(Long studyPlanId){
        StudyPlanEntity studyPlan = getStudyPlanById(studyPlanId);
        if (studentRepository.existsByStudyPlanId(studyPlanId)){
            throw new IllegalStateException(
                    "No se puede eliminar el plan de estudios ya que se encuentra asociado a estudiantes"
            );
        }
        studyPlanRepository.delete(studyPlan);
    }
}
