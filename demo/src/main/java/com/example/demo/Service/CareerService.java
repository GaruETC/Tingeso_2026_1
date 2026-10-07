package com.example.demo.Service;

import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Entity.StudentEntity;
import com.example.demo.Repository.CareerRepository;
import com.example.demo.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareerService {

    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;

    public CareerService(CareerRepository careerRepository, StudentRepository studentRepository) {
        this.careerRepository = careerRepository;
        this.studentRepository = studentRepository;
    }

    public List<CareerEntity> getCareers(){
        return careerRepository.findAll();
    }

    public CareerEntity saveCareer(CareerEntity career){
        return careerRepository.save(career);
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

    public CareerEntity updateCareer(CareerEntity career){
        return careerRepository.save(career);
    }

    public void deleteCareer(Long careerId){
        CareerEntity career = getCareerById(careerId);
        if (studentRepository.existsByCareerId(careerId)){
            throw new IllegalStateException(
                "No se puede eliminar la carrera ya que tiene estudiantes"
            );
        }
        careerRepository.delete(career);
    }
}
