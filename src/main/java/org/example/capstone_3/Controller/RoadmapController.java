package org.example.capstone_3.Controller;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.Service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(roadmapService.getRoadmapById(id));
    }

    @GetMapping("/get/student/{studentId}")
    public ResponseEntity<?> getByStudentId(@PathVariable Integer studentId) {
        return ResponseEntity.ok(roadmapService.getRoadmapsByStudentId(studentId));
    }

    @PostMapping("/add/{studentId}")
    public ResponseEntity<?> createRoadmap(@PathVariable Integer studentId) {
        return ResponseEntity.ok(roadmapService.createRoadmap(studentId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoadmap(@PathVariable Integer id) {
        return ResponseEntity.ok(roadmapService.updateRoadmap(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.ok(new ApiResponse("Roadmap deleted successfully"));
    }
}
