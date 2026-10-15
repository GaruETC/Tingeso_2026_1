package com.example.demo.Service;

import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudentRepository studentRepository;

    public StudyPlanService(StudyPlanRepository studyPlanRepository, StudentRepository studentRepository) {
        this.studyPlanRepository = studyPlanRepository;
        this.studentRepository = studentRepository;
    }

    public List<StudyPlanEntity> getStudyPlans(){
        return studyPlanRepository.findAll();
    }

    public StudyPlanEntity getStudyPlanById(Long studyPlanId){
        return studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new RuntimeException(
                "No se encontró el plan de estudio con id: " + studyPlanId
        ));
    }

    public List<StudyPlanEntity> getStudyPlansByCareerId(Long careerId){
        return studyPlanRepository.findByCareerId(careerId);
    }

    public StudyPlanEntity createStudyPlan(Long careerId){
        studyPlanRepository.findByCareerIdAndStatus(careerId, StudyPlanStatus.CURRENT)
                        .ifPresent(previousPlan -> {
                            previousPlan.setStatus(StudyPlanStatus.NOT_CURRENT);
                            studyPlanRepository.save(previousPlan);
                        });

        StudyPlanEntity studyPlan = new StudyPlanEntity();
        studyPlan.setCareerId(careerId);
        studyPlan.setStatus(StudyPlanStatus.CURRENT);
        return studyPlanRepository.save(studyPlan);
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
