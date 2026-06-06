package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.LearningGroupDTOIN;
import org.example.capstone_3.DTO.OUT.LearningGroupDTOOUT;
import org.example.capstone_3.DTO.OUT.StudentSummaryDTOOut;
import org.example.capstone_3.Service.LearningGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-group")
@RequiredArgsConstructor
public class LearningGroupController {
    private final LearningGroupService learningGroupService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(learningGroupService.getAllLearningGroups());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(learningGroupService.getLearningGroupById(id));
    }

    @PostMapping("/add/{studentId}")
    public ResponseEntity<?> add(@PathVariable Integer studentId, @RequestBody @Valid LearningGroupDTOIN dto) {
        learningGroupService.createLearningGroup(studentId, dto);
        return ResponseEntity.ok(new ApiResponse("Learning group created successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody @Valid LearningGroupDTOIN dto) {
        learningGroupService.updateLearningGroup(id, dto);
        return ResponseEntity.ok(new ApiResponse("Learning group updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        learningGroupService.deleteLearningGroup(id);
        return ResponseEntity.ok(new ApiResponse("Learning group deleted successfully"));
    }

    @PostMapping("/join-private/{student_id}/{code}")
    public ResponseEntity<?> joinPrivateGroup(@PathVariable Integer student_id, @PathVariable String code) {
        learningGroupService.joinPrivateGroup(student_id, code);
        return ResponseEntity.ok(new ApiResponse("Joined private group successfully"));
    }

    @PostMapping("/join-public/{student_id}/{group_id}")
    public ResponseEntity<?> joinPublicGroup(@PathVariable Integer student_id, @PathVariable Integer group_id) {
        learningGroupService.joinPublicGroup(student_id, group_id);
        return ResponseEntity.ok(new ApiResponse("Joined public group successfully"));
    }

    @DeleteMapping("/leave/{student_id}/{group_id}")
    public ResponseEntity<?> leaveGroup(@PathVariable Integer student_id, @PathVariable Integer group_id) {
        learningGroupService.leaveGroup(student_id, group_id);
        return ResponseEntity.ok(new ApiResponse("Left group successfully"));
    }

    @PostMapping("/invite/{inviter_id}/{invited_student_id}/{group_id}")
    public ResponseEntity<?> inviteStudent(@PathVariable Integer inviter_id, @PathVariable Integer invited_student_id, @PathVariable Integer group_id) {
        learningGroupService.inviteStudentToPrivateGroup(inviter_id, invited_student_id, group_id);
        return ResponseEntity.ok(new ApiResponse("Invitation sent successfully"));
    }

    @GetMapping("/{learningGroupId}/members")
    public ResponseEntity<?> groupMembers(@PathVariable Integer learningGroupId) {
        return ResponseEntity.ok(learningGroupService.groupMembers(learningGroupId));
    }

    @GetMapping("/student/{studentId}/groups")
    public ResponseEntity<?> studentGroups(@PathVariable Integer studentId) {
        return ResponseEntity.ok(learningGroupService.studentGroups(studentId));
    }
}
