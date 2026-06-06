package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.RoadmapStepDTOIN;
import org.example.capstone_3.Service.RoadmapStepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roadmap-step")
@RequiredArgsConstructor
public class RoadmapStepController {
    private final RoadmapStepService roadmapStepService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(roadmapStepService.getAllRoadmapSteps());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(roadmapStepService.getRoadmapStepById(id));
    }

    @PostMapping("/add/{roadmapId}")
    public ResponseEntity<?> add(@PathVariable Integer roadmapId, @RequestBody @Valid RoadmapStepDTOIN dto) {
        roadmapStepService.addRoadmapStep(roadmapId, dto);
        return ResponseEntity.ok(new ApiResponse("Roadmap step created successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody @Valid RoadmapStepDTOIN dto) {
        roadmapStepService.updateRoadmapStep(id, dto);
        return ResponseEntity.ok(new ApiResponse("Roadmap step updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        roadmapStepService.deleteRoadmapStep(id);
        return ResponseEntity.ok(new ApiResponse("Roadmap step deleted successfully"));
    }

    @PutMapping("/complete/{student_id}/{roadmap_id}/{step_id}")
    public ResponseEntity<?> completeStep(@PathVariable Integer student_id, @PathVariable Integer roadmap_id, @PathVariable Integer step_id) {
        roadmapStepService.completeStep(student_id, roadmap_id, step_id);
        return ResponseEntity.ok(new ApiResponse("Step completed successfully"));
    }

    @GetMapping("/next/{student_id}/{roadmap_id}")
    public ResponseEntity<?> getNextStep(@PathVariable Integer student_id, @PathVariable Integer roadmap_id) {
        return ResponseEntity.ok(roadmapStepService.getNextStep(student_id, roadmap_id));
    }
}
