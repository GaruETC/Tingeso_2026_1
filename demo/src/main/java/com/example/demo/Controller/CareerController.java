package com.example.demo.Controller;

import com.example.demo.DTO.CareerDto;
import com.example.demo.Entity.CareerEntity;
import com.example.demo.Entity.Enum.CareerStatus;
import com.example.demo.Service.CareerService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping
    public ResponseEntity<List<CareerEntity>> getCareers(@RequestParam(required = false) CareerStatus status){
        List<CareerEntity> careers;
        if (status != null){
            careers = careerService.getCareersByStatus(status);
        } else{
            careers = careerService.getCareers();
        }
        return ResponseEntity.ok(careers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareerEntity> getCareerById(@PathVariable Long id){
        return ResponseEntity.ok(careerService.getCareerById(id));
    }

    @PostMapping
    public ResponseEntity<CareerEntity> createCareer(@RequestBody CareerDto dto){
        CareerEntity created = careerService.createCareer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareerEntity> updateCareer(
            @PathVariable Long id, @RequestBody CareerDto dto){
        CareerEntity updated = careerService.updateCareer(id, dto);
        return ResponseEntity.ok(updated);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCareer(@PathVariable Long id){
        careerService.deleteCareer(id);
        return ResponseEntity.noContent().build();
    }
}
