package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Review findReviewById(Integer id);

    List<Review> findReviewsByMentorId(Integer mentorId);

    Review findReviewByMockInterviewId(Integer mockInterviewId);

    List<Review> findReviewsByStudentId(Integer studentId);
}