package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.AdminDTOIn;
import org.example.capstone_3.DTO.IN.SkillCategoryDTOIn;
import org.example.capstone_3.Service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(adminService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveAdmin(@RequestBody @Valid AdminDTOIn adminDTOIn) {
        adminService.create(adminDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Admin has been saved successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Integer id, @RequestBody @Valid AdminDTOIn adminDTOIn) {
        adminService.update(id, adminDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Admin has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Integer id) {
        adminService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Admin has been deleted successfully"));
    }

    @PostMapping("/{adminId}/skills/generate-by-category")
    public ResponseEntity<?> generateSkillsByCategory(
            @PathVariable Integer adminId,
            @RequestBody @Valid SkillCategoryDTOIn skillCategoryDTOIn) {
        return ResponseEntity.ok(adminService.generateSkillsFromCategory(adminId, skillCategoryDTOIn));
    }

    @GetMapping("/{adminId}/mentor/get")
    public ResponseEntity<?> getAllMentors(@PathVariable Integer adminId) {
        return ResponseEntity.ok(adminService.getAllMentors(adminId));
    }

    @GetMapping("/{adminId}/mentor/pending")
    public ResponseEntity<?> getPendingMentors(@PathVariable Integer adminId) {
        return ResponseEntity.ok(adminService.getPendingMentors(adminId));
    }

    @GetMapping("/{adminId}/mentor/get/{mentorId}")
    public ResponseEntity<?> getMentorById(
            @PathVariable Integer adminId,
            @PathVariable Integer mentorId) {
        return ResponseEntity.ok(adminService.getMentorById(adminId, mentorId));
    }

    @PutMapping("/{adminId}/mentor/{mentorId}/approve")
    public ResponseEntity<?> approveMentor(
            @PathVariable Integer adminId,
            @PathVariable Integer mentorId) {
        adminService.approveMentor(adminId, mentorId);
        return ResponseEntity.ok().body(new ApiResponse("Mentor has been approved successfully"));
    }

    @PutMapping("/{adminId}/mentor/{mentorId}/unapprove")
    public ResponseEntity<?> unapproveMentor(
            @PathVariable Integer adminId,
            @PathVariable Integer mentorId) {
        adminService.unapproveMentor(adminId, mentorId);
        return ResponseEntity.ok().body(new ApiResponse("Mentor approval has been revoked successfully"));
    }
}
