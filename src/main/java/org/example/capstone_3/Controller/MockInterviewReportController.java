package org.example.capstone_3.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiResponse;
import org.example.capstone_3.DTO.IN.MockInterviewReportDTOIN;
import org.example.capstone_3.Service.MockInterviewReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-interview-report")
@RequiredArgsConstructor
public class MockInterviewReportController {

    private final MockInterviewReportService mockInterviewReportService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(mockInterviewReportService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMockInterviewReportById(@PathVariable Integer id) {
        return ResponseEntity.ok(mockInterviewReportService.getById(id));
    }

    @GetMapping("/ai/{studentId}/{mockInterviewId}")
    public ResponseEntity<?> getAiReport(@PathVariable Integer studentId,
                                         @PathVariable Integer mockInterviewId) {
        return ResponseEntity.ok(
                mockInterviewReportService.getAiReport(studentId, mockInterviewId)
        );
    }

    @PostMapping("/add/{mentorId}/{mockInterviewId}")
    public ResponseEntity<?> saveMockInterviewReport(@PathVariable Integer mentorId,
                                                     @PathVariable Integer mockInterviewId,
                                                     @RequestBody @Valid MockInterviewReportDTOIN dto) {
        mockInterviewReportService.create(mentorId, mockInterviewId, dto);
        return ResponseEntity.ok().body(new ApiResponse("Mock interview report has been created and sent to student successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMockInterviewReport(@PathVariable Integer id,
                                                       @RequestBody @Valid MockInterviewReportDTOIN dto) {
        mockInterviewReportService.update(id, dto);
        return ResponseEntity.ok().body(new ApiResponse("Mock interview report has been updated and resent to student successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMockInterviewReport(@PathVariable Integer id) {
        mockInterviewReportService.delete(id);
        return ResponseEntity.ok().body(new ApiResponse("Mock interview report has been deleted successfully"));
    }
}