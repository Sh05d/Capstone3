package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.MentorDTOIn;
import org.example.capstone_3.Service.MentorService;
import org.example.capstone_3.Service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final ReviewService reviewService;

    @GetMapping("/get")
    public ResponseEntity<?> getApprovedMentors() {
        return ResponseEntity.ok(mentorService.getApprovedMentors());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getApprovedMentorById(@PathVariable Integer id) {
        return ResponseEntity.ok(mentorService.getApprovedById(id));
    }

    @GetMapping("/get/{id}/reviews")
    public ResponseEntity<?> getMentorReviews(@PathVariable Integer id) {
        return ResponseEntity.ok(reviewService.getReviewsByMentorId(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveMentor(@RequestBody @Valid MentorDTOIn mentorDTOIn) {
        mentorService.create(mentorDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Mentor has been saved successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMentor(@PathVariable Integer id, @RequestBody @Valid MentorDTOIn mentorDTOIn) {
        mentorService.update(id, mentorDTOIn);
        return ResponseEntity.ok().body(new ApiResponse("Mentor has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMentor(@PathVariable Integer id) {
        mentorService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Mentor has been deleted successfully"));
    }
}