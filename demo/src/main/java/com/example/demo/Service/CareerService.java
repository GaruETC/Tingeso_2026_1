package com.example.demo.Service;

import com.example.demo.DTO.CareerDto;
import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Repository.CareerRepository;
import com.example.demo.Repository.StudentRepository;
import com.example.demo.Repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareerService {

    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;

    public CareerService(CareerRepository careerRepository, StudentRepository studentRepository, StudyPlanRepository studyPlanRepository) {
        this.careerRepository = careerRepository;
        this.studentRepository = studentRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    public List<CareerEntity> getCareers(){
        return careerRepository.findAll();
    }

    public CareerEntity getCareerById(Long id){
        return careerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la carrera con id: " + id
                ));
    }

    public CareerEntity getCareerByCode(String code){
        return careerRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la carrera con codigo: " + code
                ));
    }

    public CareerEntity getCareerByName(String name){
        return careerRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la carrera con ese nombre: " + name
                ));
    }

    public List<CareerEntity> getCareersByStatus(CareerStatus status){
        return careerRepository.findByStatus(status);
    }

    public CareerEntity createCareer(CareerDto careerDto){
        if (careerRepository.existsByCode(careerDto.code())){
            throw new IllegalStateException(
                    "Ya existe una carrera con el codigo: " + careerDto .code()
            );
        }
        CareerEntity career = new CareerEntity();
        career.setCode(careerDto.code());
        career.setDescription(careerDto.description());
        career.setName(careerDto.name());
        career.setStatus(careerDto.status());

        return careerRepository.save(career);
    }

    public CareerEntity updateCareer(Long id, CareerDto careerDto){
        CareerEntity career = careerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("No existe la carrera con id: " + id));

        boolean isLinked = studentRepository.existsByCareerId(id) || studyPlanRepository.existsByCareerId(id);

        if (isLinked){
            boolean changeCode = !career.getCode().equals(careerDto.code());
            boolean changeName = !career.getName().equals(careerDto.name());
            boolean changeDescription = !career.getDescription().equals(careerDto.description());

            if (changeCode || changeName || changeDescription) {
                throw new IllegalStateException(
                        "Si la carrera tiene estudiantes o planes de estudio asociados, solo se puede modificar el estado"
                );
            }

            career.setStatus(careerDto.status());
        } else{
            if (careerRepository.existsByCodeAndIdNot(careerDto.code(), id)){
                throw new IllegalStateException("Ya existe una carrera con con el codigo: " + careerDto.code());
            }

            career.setCode(careerDto.code());
            career.setName(careerDto.name());
            career.setDescription(careerDto.description());
            career.setStatus(careerDto.status());
        }

        return careerRepository.save(career);
    }

    public void deleteCareer(Long careerId){
        CareerEntity career = getCareerById(careerId);
        if (studentRepository.existsByCareerId(careerId)){
            throw new IllegalStateException(
                    "No se puede eliminar la carrera ya que tiene estudiantes"
            );
        }
        if (studyPlanRepository.existsByCareerId(careerId)){
            throw new IllegalStateException(
                    "No se puede eliminar la carrera ya que tiene un plan de estudios asociado"
            );
        }
        careerRepository.delete(career);
    }
}
