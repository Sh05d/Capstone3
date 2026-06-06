package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    Challenge findChallengeById(Integer id);

    @Query("select c from Challenge c where c.skill.name=?1")
    List<Challenge> findChallengesBySkillName(String skillName);

    @Query("select c from Challenge c where c.skill.id=?1")
    List<Challenge> availableChallengesBySkillId(Integer skillId);

    @Query("select c from Challenge c where c.skill.id=?1 and c.difficulty=?2")
    List<Challenge> availableChallengesBySkillIdAndDifficulty(Integer skillId, String difficulty);
}
