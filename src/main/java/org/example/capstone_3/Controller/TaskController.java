package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.TaskDTOIN;
import org.example.capstone_3.DTO.OUT.TaskDTOOUT;
import org.example.capstone_3.Service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping("/add/{learningGroupId}")
    public ResponseEntity<?> add(@PathVariable Integer learningGroupId) {
        taskService.addTask(learningGroupId);
        return ResponseEntity.ok(new ApiResponse("Task created successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody @Valid TaskDTOIN dto) {
        taskService.updateTask(id, dto);
        return ResponseEntity.ok(new ApiResponse("Task updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(new ApiResponse("Task deleted successfully"));
    }

    @GetMapping("/{learningGroupId}/unsubmitted-tasks/{studentId}")
    public ResponseEntity<?> unsubmittedTasksForStudent(@PathVariable Integer learningGroupId, @PathVariable Integer studentId) {
        return ResponseEntity.ok(taskService.unsubmittedTasksForStudent(learningGroupId , studentId));
    }

    @GetMapping("/{learningGroupId}/tasks/old")
    public ResponseEntity<?> groupOldTasks(@PathVariable Integer learningGroupId) {
        return ResponseEntity.ok(taskService.groupOldTasks(learningGroupId));
    }

    @GetMapping("/{learningGroupId}/tasks/available")
    public ResponseEntity<?> groupAvailableTasks(@PathVariable Integer learningGroupId) {
        return ResponseEntity.ok(taskService.groupAvailableTasks(learningGroupId));
    }
}
