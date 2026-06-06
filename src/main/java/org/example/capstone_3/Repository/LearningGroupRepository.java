package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.LearningGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningGroupRepository extends JpaRepository<LearningGroup,Integer> {
    LearningGroup findLearningGroupById(Integer id);

    LearningGroup findLearningGroupByCode(String code);

    @Query("select g from LearningGroup g join g.students s where s.id = ?1")
    List<LearningGroup> studentGroups(Integer studentId);
}
