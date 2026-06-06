package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.ReviewDTOIN;
import org.example.capstone_3.DTO.OUT.ReviewDTOOUT;
import org.example.capstone_3.DTO.OUT.ReviewableMockInterviewDTOOut;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.Review;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.MentorRepository;
import org.example.capstone_3.Repository.MockInterviewRepository;
import org.example.capstone_3.Repository.ReviewRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final MockInterviewRepository mockInterviewRepository;

    public ReviewDTOOUT createByStudent(Integer studentId, Integer mockInterviewId, ReviewDTOIN dto) {

        Student student = findStudent(studentId);
        MockInterview mockInterview = findMockInterview(mockInterviewId);

        validateReviewableMockInterview(student, mockInterview);

        if (reviewRepository.findReviewByMockInterviewId(mockInterviewId) != null) {
            throw new ApiException("This mock interview already has a review");
        }

        Mentor mentor = mockInterview.getMentor();

        Review review = new Review();

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setStudent(student);
        review.setMentor(mentor);
        review.setMockInterview(mockInterview);

        reviewRepository.save(review);

        updateMentorRating(mentor);

        return toDtoOut(review);
    }

    public ReviewDTOOUT getById(Integer id) {

        Review review = findReview(id);

        return toDtoOut(review);
    }

    public List<ReviewDTOOUT> getAll() {

        List<Review> reviews = reviewRepository.findAll();

        List<ReviewDTOOUT> reviewDTOOUTS = new ArrayList<>();

        for (Review review : reviews) {
            reviewDTOOUTS.add(toDtoOut(review));
        }

        return reviewDTOOUTS;
    }

    public List<ReviewDTOOUT> getReviewsByMentorId(Integer mentorId) {

        findMentor(mentorId);

        List<Review> reviews = reviewRepository.findReviewsByMentorId(mentorId);

        List<ReviewDTOOUT> reviewDTOOUTS = new ArrayList<>();

        for (Review review : reviews) {
            reviewDTOOUTS.add(toDtoOut(review));
        }

        return reviewDTOOUTS;
    }

    public List<ReviewDTOOUT> getReviewsByStudentId(Integer studentId) {

        findStudent(studentId);

        List<Review> reviews = reviewRepository.findReviewsByStudentId(studentId);

        List<ReviewDTOOUT> reviewDTOOUTS = new ArrayList<>();

        for (Review review : reviews) {
            reviewDTOOUTS.add(toDtoOut(review));
        }

        return reviewDTOOUTS;
    }

    public List<ReviewableMockInterviewDTOOut> getReviewableMockInterviews(Integer studentId) {

        findStudent(studentId);

        List<MockInterview> mentorInterviews =
                mockInterviewRepository.findMockInterviewsByStudentIdAndInterviewMode(studentId, "MENTOR");

        List<ReviewableMockInterviewDTOOut> result = new ArrayList<>();

        for (MockInterview mockInterview : mentorInterviews) {

            if (!canBeReviewedNow(mockInterview)) {
                continue;
            }

            completeScheduledInterviewIfEnded(mockInterview);

            Review existingReview = reviewRepository.findReviewByMockInterviewId(mockInterview.getId());

            Mentor mentor = mockInterview.getMentor();

            result.add(new ReviewableMockInterviewDTOOut(
                    mockInterview.getId(),
                    mentor.getId(),
                    mentor.getFullName(),
                    mockInterview.getInterviewType(),
                    mockInterview.getScheduledAt(),
                    mockInterview.getScore(),
                    existingReview != null
            ));
        }

        return result;
    }

    public ReviewDTOOUT updateByStudent(Integer studentId, Integer reviewId, ReviewDTOIN dto) {

        Review review = findReview(reviewId);

        if (review.getStudent() == null || !review.getStudent().getId().equals(studentId)) {
            throw new ApiException("This review does not belong to this student");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        reviewRepository.save(review);

        updateMentorRating(review.getMentor());

        return toDtoOut(review);
    }

    public void update(Integer id, ReviewDTOIN dto) {

        Review review = findReview(id);

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        reviewRepository.save(review);

        updateMentorRating(review.getMentor());
    }

    public void delete(Integer id) {

        Review review = findReview(id);

        Mentor mentor = review.getMentor();

        reviewRepository.delete(review);

        updateMentorRating(mentor);
    }

    private void validateReviewableMockInterview(Student student, MockInterview mockInterview) {

        if (!"MENTOR".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("Review is allowed only for mentor mock interviews");
        }

        if (mockInterview.getStudent() == null ||
                !mockInterview.getStudent().getId().equals(student.getId())) {
            throw new ApiException("This mock interview does not belong to this student");
        }

        if (mockInterview.getMentor() == null) {
            throw new ApiException("This mock interview is not linked to a mentor");
        }

        completeScheduledInterviewIfEnded(mockInterview);

        if (!"COMPLETE".equals(mockInterview.getStatus())) {
            throw new ApiException("Student can review mentor only after completed interview");
        }
    }

    private void completeScheduledInterviewIfEnded(MockInterview mockInterview) {

        if ("COMPLETE".equals(mockInterview.getStatus())) {
            return;
        }

        if (!"SCHEDULE".equals(mockInterview.getStatus())) {
            throw new ApiException("Student can review mentor only after scheduled interview is ended");
        }

        if (mockInterview.getScheduledAt() == null || mockInterview.getDurationMinutes() == null) {
            throw new ApiException("Mock interview schedule is incomplete");
        }

        LocalDateTime interviewEndTime =
                mockInterview.getScheduledAt().plusMinutes(mockInterview.getDurationMinutes());

        if (LocalDateTime.now().isBefore(interviewEndTime)) {
            throw new ApiException("Interview time has not ended yet. Student cannot review mentor now");
        }

        mockInterview.setStatus("COMPLETE");

        mockInterviewRepository.save(mockInterview);
    }

    private boolean canBeReviewedNow(MockInterview mockInterview) {

        if (!"MENTOR".equals(mockInterview.getInterviewMode())) {
            return false;
        }

        if (mockInterview.getMentor() == null) {
            return false;
        }

        if ("COMPLETE".equals(mockInterview.getStatus())) {
            return true;
        }

        if (!"SCHEDULE".equals(mockInterview.getStatus())) {
            return false;
        }

        if (mockInterview.getScheduledAt() == null || mockInterview.getDurationMinutes() == null) {
            return false;
        }

        LocalDateTime interviewEndTime =
                mockInterview.getScheduledAt().plusMinutes(mockInterview.getDurationMinutes());

        return !LocalDateTime.now().isBefore(interviewEndTime);
    }

    private Student findStudent(Integer studentId) {

        Student student = studentRepository.findStudentById(studentId);

        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        return student;
    }

    private Mentor findMentor(Integer mentorId) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);

        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        return mentor;
    }

    private MockInterview findMockInterview(Integer mockInterviewId) {

        MockInterview mockInterview =
                mockInterviewRepository.findMockInterviewById(mockInterviewId);

        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        return mockInterview;
    }

    private Review findReview(Integer id) {

        Review review = reviewRepository.findReviewById(id);

        if (review == null) {
            throw new ApiException("Review with id " + id + " not found");
        }

        return review;
    }

    private void updateMentorRating(Mentor mentor) {

        if (mentor == null) {
            return;
        }

        List<Review> reviews = reviewRepository.findReviewsByMentorId(mentor.getId());

        if (reviews == null || reviews.isEmpty()) {
            mentor.setRating(0.0);
            mentorRepository.save(mentor);
            return;
        }

        int totalRating = 0;

        for (Review review : reviews) {
            totalRating = totalRating + review.getRating();
        }

        double averageRating = (double) totalRating / reviews.size();

        mentor.setRating(averageRating);

        mentorRepository.save(mentor);
    }

    private ReviewDTOOUT toDtoOut(Review review) {

        Integer studentId = review.getStudent() != null
                ? review.getStudent().getId()
                : null;

        Integer mentorId = review.getMentor() != null
                ? review.getMentor().getId()
                : null;

        Integer mockInterviewId = review.getMockInterview() != null
                ? review.getMockInterview().getId()
                : null;

        return new ReviewDTOOUT(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                studentId,
                mentorId,
                mockInterviewId
        );
    }
}