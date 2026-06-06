package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.ChallengeAttemptDTOIN;
import org.example.capstone_3.Service.ChallengeAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenge-attempt")
@RequiredArgsConstructor
public class ChallengeAttemptController {

    private final ChallengeAttemptService challengeAttemptService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(challengeAttemptService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getChallengeAttemptById(@PathVariable Integer id) {
        return ResponseEntity.ok(challengeAttemptService.getById(id));
    }

    @PostMapping("{student_id}/create-attempt/{challenge_id}")
    public ResponseEntity<?> saveChallengeAttempt(@PathVariable Integer student_id,@PathVariable Integer challenge_id, @RequestBody @Valid ChallengeAttemptDTOIN challengeAttemptDTOIN) {
        challengeAttemptService.create(student_id,challenge_id, challengeAttemptDTOIN);
        return ResponseEntity.ok().body(new ApiResponse("Challenge attempt has been saved successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChallengeAttempt(@PathVariable Integer id, @RequestBody @Valid ChallengeAttemptDTOIN challengeAttemptDTOIN) {
        challengeAttemptService.update(id, challengeAttemptDTOIN);
        return ResponseEntity.ok().body(new ApiResponse("Challenge attempt has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChallengeAttempt(@PathVariable Integer id) {
        challengeAttemptService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Challenge attempt has been deleted successfully"));
    }

    @GetMapping("/student-attempts/{studentId}/{challengeId}")
    public ResponseEntity<?> studentAttemptsForChallenge(@PathVariable Integer studentId, @PathVariable Integer challengeId) {
        return ResponseEntity.ok(challengeAttemptService.studentAttemptsForChallenge(studentId, challengeId));
    }
}
