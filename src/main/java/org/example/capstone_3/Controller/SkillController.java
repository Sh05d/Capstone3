package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.SkillDTOIn;
import org.example.capstone_3.Service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(skillService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Integer id) {
        return ResponseEntity.ok(skillService.getById(id));
    }

    @PostMapping("/add/{adminId}")
    public ResponseEntity<?> saveSkill(@PathVariable Integer adminId, @RequestBody @Valid SkillDTOIn skillDTOIn) {
        skillService.create(adminId, skillDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Skill has been saved successfully"));
    }

    @PutMapping("/update/{adminId}/{skillId}")
    public ResponseEntity<?> updateSkill(@PathVariable Integer adminId, @PathVariable Integer skillId, @RequestBody @Valid SkillDTOIn skillDTOIn) {
        skillService.update(adminId, skillId, skillDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Skill has been updated successfully"));
    }

    @DeleteMapping("/delete/{adminId}/{skillId}")
    public ResponseEntity<?> deleteSkill(@PathVariable Integer adminId, @PathVariable Integer skillId) {
        skillService.delete(adminId, skillId);
        return ResponseEntity.ok().body(new ApiResponse("Skill has been deleted successfully"));
    }
}
