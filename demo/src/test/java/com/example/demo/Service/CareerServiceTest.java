package com.example.demo.Service;

import com.example.demo.DTO.CareerDto;
import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Repository.CareerRepository;
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
class CareerServiceTest {

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @InjectMocks
    private CareerService careerService;

    private CareerEntity career;
    private CareerDto careerDto;

    @BeforeEach
    void setUp() {
        career = new CareerEntity();
        career.setId(1L);
        career.setCode("COMP-101");
        career.setName("Ingenieria en Computacion");
        career.setDescription("Carrera de ingenieria civil");
        career.setStatus(CareerStatus.ACTIVO);

        careerDto = new CareerDto(
                "COMP-101",
                "Ingenieria en Computacion",
                "Carrera de ingenieria civil",
                CareerStatus.ACTIVO
        );
    }

    @Test
    void whenGetCareers_thenReturnList() {
        //Given
        when(careerRepository.findAll()).thenReturn(List.of(career));

        //When
        List<CareerEntity> result = careerService.getCareers();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCode()).isEqualTo("COMP-101");
    }

    @Test
    void whenGetCareerById_thenReturnCareer() {
        //Given
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));

        //When
        CareerEntity result = careerService.getCareerById(1L);

        //Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetCareerByCode_thenReturnCareer() {
        //Given
        when(careerRepository.findByCode("COMP-101")).thenReturn(Optional.of(career));

        //When
        CareerEntity result = careerService.getCareerByCode("COMP-101");

        //Then
        assertThat(result.getCode()).isEqualTo("COMP-101");
    }

    @Test
    void whenGetCareerByName_thenReturnCareer() {
        //Given
        when(careerRepository.findByName("Ingenieria en Computacion")).thenReturn(Optional.of(career));

        //When
        CareerEntity result = careerService.getCareerByName("Ingenieria en Computacion");

        //Then
        assertThat(result.getName()).isEqualTo("Ingenieria en Computacion");
    }

    @Test
    void whenGetCareersByStatus_thenReturnList() {
        //Given
        when(careerRepository.findByStatus(CareerStatus.ACTIVO)).thenReturn(List.of(career));

        //When
        List<CareerEntity> result = careerService.getCareersByStatus(CareerStatus.ACTIVO);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(CareerStatus.ACTIVO);
    }

    @Test
    void whenCreateCareer_thenReturnSavedCareer() {
        //Given
        when(careerRepository.existsByCode("COMP-101")).thenReturn(false);
        when(careerRepository.save(any(CareerEntity.class))).thenReturn(career);

        //When
        CareerEntity result = careerService.createCareer(careerDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("COMP-101");
    }

    @Test
    void whenCreateCareerExistingCode_thenThrowException() {
        //Given
        when(careerRepository.existsByCode("COMP-101")).thenReturn(true);

        //When / Then
        assertThatThrownBy(() -> careerService.createCareer(careerDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe una carrera con el codigo");
    }

    @Test
    void whenUpdateCareerNotFound_thenThrowException() {
        //Given
        when(careerRepository.findById(99L)).thenReturn(Optional.empty());

        //When / Then
        assertThatThrownBy(() -> careerService.updateCareer(99L, careerDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No existe la carrera con id");
    }

    @Test
    void whenUpdateCareerLinkedChangeStatusOnly_thenReturnUpdatedCareer() {
        //Given
        CareerDto statusDto = new CareerDto(
                "COMP-101",
                "Ingenieria en Computacion",
                "Carrera de ingenieria civil",
                CareerStatus.INACTIVO
        );
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studentRepository.existsByCareerId(1L)).thenReturn(true);
        when(careerRepository.save(any(CareerEntity.class))).thenReturn(career);

        //When
        CareerEntity result = careerService.updateCareer(1L, statusDto);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void whenUpdateCareerNotLinked_thenReturnUpdatedCareer() {
        //Given
        CareerDto changedDto = new CareerDto(
                "NUEVO-101",
                "Nueva Ingenieria",
                "Nueva descripcion",
                CareerStatus.ACTIVO
        );
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studentRepository.existsByCareerId(1L)).thenReturn(false);
        when(studyPlanRepository.existsByCareerId(1L)).thenReturn(false);
        when(careerRepository.existsByCodeAndIdNot("NUEVO-101", 1L)).thenReturn(false);
        when(careerRepository.save(any(CareerEntity.class))).thenReturn(career);

        //When
        CareerEntity result = careerService.updateCareer(1L, changedDto);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void whenDeleteCareer_thenVerifyDeletion() {
        //Given
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career));
        when(studentRepository.existsByCareerId(1L)).thenReturn(false);
        when(studyPlanRepository.existsByCareerId(1L)).thenReturn(false);

        //When
        careerService.deleteCareer(1L);

        //Then
        verify(careerRepository).delete(career);
    }
}