package com.example.demo.Service;

import com.example.demo.DTO.StudyPlanDto;
import com.example.demo.Entity.Enum.StudyPlanStatus;
import com.example.demo.Entity.StudyPlanEntity;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudyPlanService studyPlanService;

    private StudyPlanEntity studyPlan;
    private StudyPlanDto studyPlanDto;

    @BeforeEach
    void setUp() {
        studyPlan = new StudyPlanEntity();
        studyPlan.setId(1L);
        studyPlan.setCareerId(10L);
        studyPlan.setCode("PLAN-2026");
        studyPlan.setStatus(StudyPlanStatus.VIGENTE);

        studyPlanDto = new StudyPlanDto(10L, "PLAN-2027");
    }

    @Test
    void whenGetStudyPlans_thenReturnAllPlans() {
        //Given
        when(studyPlanRepository.findAll()).thenReturn(List.of(studyPlan));

        //When
        List<StudyPlanEntity> result = studyPlanService.getStudyPlans();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCode()).isEqualTo("PLAN-2026");
    }

    @Test
    void whenGetStudyPlanById_thenReturnPlan() {
        //Given
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(studyPlan));

        //When
        StudyPlanEntity result = studyPlanService.getStudyPlanById(1L);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetStudyPlansByCareerId_thenReturnFilteredPlans() {
        //Given
        when(studyPlanRepository.findByCareerId(10L)).thenReturn(List.of(studyPlan));

        //When
        List<StudyPlanEntity> result = studyPlanService.getStudyPlansByCareerId(10L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCareerId()).isEqualTo(10L);
    }

    @Test
    void whenCreateStudyPlanWithoutPrevious_thenReturnSavedPlan() {
        //Given
        when(studyPlanRepository.findByCareerIdAndStatus(10L, StudyPlanStatus.VIGENTE))
                .thenReturn(Optional.empty());
        when(studyPlanRepository.save(any(StudyPlanEntity.class))).thenReturn(studyPlan);

        //When
        StudyPlanEntity result = studyPlanService.createStudyPlan(studyPlanDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StudyPlanStatus.VIGENTE);
    }

    @Test
    void whenCreateStudyPlanWithPrevious_thenReturnSavedPlanAndInvalidatePrevious() {
        //Given
        StudyPlanEntity previousPlan = new StudyPlanEntity();
        previousPlan.setId(2L);
        previousPlan.setStatus(StudyPlanStatus.VIGENTE);

        when(studyPlanRepository.findByCareerIdAndStatus(10L, StudyPlanStatus.VIGENTE))
                .thenReturn(Optional.of(previousPlan));

        when(studyPlanRepository.save(any(StudyPlanEntity.class))).thenReturn(studyPlan);

        //When
        StudyPlanEntity result = studyPlanService.createStudyPlan(studyPlanDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(previousPlan.getStatus()).isEqualTo(StudyPlanStatus.NO_VIGENTE);
        verify(studyPlanRepository).save(previousPlan);
    }

    @Test
    void whenDeleteStudyPlan_thenVerifyDeletion() {
        //Given
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(studyPlan));
        when(studentRepository.existsByStudyPlanId(1L)).thenReturn(false);

        //When
        studyPlanService.deleteStudyPlan(1L);

        //Then
        verify(studyPlanRepository).delete(studyPlan);
    }
}