package com.example.demo.Service;

import com.example.demo.Entity.AcademicHistorialEntity;
import com.example.demo.Entity.Enum.AcademicHistorialStatus;
import com.example.demo.Repository.AcademicHistorialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicHistorialServiceTest {

    @Mock
    private AcademicHistorialRepository academicHistorialRepository;

    @InjectMocks
    private AcademicHistorialService academicHistorialService;

    private AcademicHistorialEntity historial;

    @BeforeEach
    void setUp() {
        historial = new AcademicHistorialEntity();
        historial.setId(1L);
        historial.setStudentId(10L);
        historial.setSubjectId(20L);
        historial.setAcademicTermId(30L);
        historial.setFinalGrade(5.5f);
        historial.setStatus(AcademicHistorialStatus.APROBADO);
    }

    @Test
    void whenGetAcademicTerms_thenReturnList() {
        //Given
        when(academicHistorialRepository.findByStudentId(10L)).thenReturn(List.of(historial));

        //When
        List<AcademicHistorialEntity> result = academicHistorialService.getAcademicTerms(10L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStudentId()).isEqualTo(10L);
    }

    @Test
    void whenGetPassedSubjectsByStudent_thenReturnList() {
        //Given
        when(academicHistorialRepository.findByStudentIdAndStatus(10L, AcademicHistorialStatus.APROBADO)).thenReturn(List.of(historial));

        //When
        List<AcademicHistorialEntity> result = academicHistorialService.getPassedSubjectsByStudent(10L);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(AcademicHistorialStatus.APROBADO);
    }
}