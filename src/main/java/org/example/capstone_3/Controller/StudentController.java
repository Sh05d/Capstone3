package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.StudentCvDTOIn;
import org.example.capstone_3.DTO.IN.StudentDTOIn;
import org.example.capstone_3.DTO.IN.StudentGithubDTOIn;
import org.example.capstone_3.Service.RoadmapStepService;
import org.example.capstone_3.Service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final RoadmapStepService roadmapStepService;

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(studentService.getLeaderboard());
    }

    @GetMapping("/leaderboard/rank/{id}")
    public ResponseEntity<?> getLeaderboardRank(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getLeaderboardRank(id));
    }

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(studentService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping("/{studentId}/roadmap/{roadmapId}/current-step")
    public ResponseEntity<?> getCurrentRoadmapStep(
            @PathVariable Integer studentId,
            @PathVariable Integer roadmapId) {
        return ResponseEntity.ok(roadmapStepService.getCurrentRoadmapStep(studentId, roadmapId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveStudent(@RequestBody @Valid StudentDTOIn studentDTOIn) {
        studentService.addStudent(studentDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Student has been saved successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Integer id, @RequestBody @Valid StudentDTOIn studentDTOIn) {
        studentService.updateStudent(id, studentDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Student has been updated successfully"));
    }

    @PutMapping("/{id}/cv")
    public ResponseEntity<?> updateStudentCv(
            @PathVariable Integer id,
            @RequestBody @Valid StudentCvDTOIn studentCvDTOIn) {
        studentService.updateCv(id, studentCvDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("CV has been updated successfully"));
    }

    @PutMapping("/{id}/github")
    public ResponseEntity<?> updateStudentGithub(
            @PathVariable Integer id,
            @RequestBody @Valid StudentGithubDTOIn studentGithubDTOIn) {
        studentService.updateGithub(id, studentGithubDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("GitHub profile has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Integer id) {
        studentService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Student has been deleted successfully"));
    }

    @PostMapping("/add-skill/{studentId}/{skillId}")
    public ResponseEntity<?> addSkillToStudent(
            @PathVariable Integer studentId,
            @PathVariable Integer skillId) {
        studentService.addSkillToStudent(studentId, skillId);
        return ResponseEntity.ok().body(new ApiResponse("Skill has been added to student successfully"));
    }

    @DeleteMapping("/remove-skill/{studentId}/{skillId}")
    public ResponseEntity<?> removeSkillFromStudent(
            @PathVariable Integer studentId,
            @PathVariable Integer skillId) {
        studentService.removeSkillFromStudent(studentId, skillId);
        return ResponseEntity.ok().body(new ApiResponse("Skill has been removed from student successfully"));
    }
}
