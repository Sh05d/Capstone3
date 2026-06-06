package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.ChallengeDTOIN;
import org.example.capstone_3.Service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(challengeService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getChallengeById(@PathVariable Integer id) {
        return ResponseEntity.ok(challengeService.getById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveChallenge(@RequestBody @Valid ChallengeDTOIN challengeDTOIN) {
        challengeService.create(challengeDTOIN);
        return ResponseEntity.ok().body(new ApiResponse("Challenge has been saved successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChallenge(@PathVariable Integer id, @RequestBody @Valid ChallengeDTOIN challengeDTOIN) {
        challengeService.update(id, challengeDTOIN);
        return ResponseEntity.ok().body(new ApiResponse("Challenge has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChallenge(@PathVariable Integer id) {
        challengeService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Challenge has been deleted successfully"));
    }

    @GetMapping("/by-skill/{skillId}")
    public ResponseEntity<?> challengesBySkill(@PathVariable Integer skillId) {
        return ResponseEntity.ok(challengeService.challengesBySkill(skillId));
    }

    @GetMapping("/by-skill-and-difficulty/{skillId}/{difficulty}")
    public ResponseEntity<?> challengesBySkillAndDifficulty(@PathVariable Integer skillId, @PathVariable String difficulty) {
        return ResponseEntity.ok(challengeService.challengesBySkillAndDifficulty(skillId, difficulty));
    }
}
