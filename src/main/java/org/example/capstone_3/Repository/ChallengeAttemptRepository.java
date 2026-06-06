package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Integer> {

    ChallengeAttempt findChallengeAttemptById(Integer id);

    @Query("select a from ChallengeAttempt a where a.student.id=?1 and a.challenge.id=?2 and a.correct = true")
    ChallengeAttempt correctAttemptForChallenge(Integer student_id,  Integer challenge_id);

    @Query("select ca from ChallengeAttempt ca where ca.student.id = ?1 and ca.challenge.id = ?2")
    List<ChallengeAttempt> studentAttemptsForChallenge(Integer studentId, Integer challengeId);
}
