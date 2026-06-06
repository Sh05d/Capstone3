package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission,Integer> {
    TaskSubmission findTaskSubmissionById(Integer id);

    @Query("select ts from TaskSubmission ts where ts.task.id = ?1 and ts.student.id = ?2 and ts.score >= ?3")
    TaskSubmission findPassingSubmission(Integer taskId, Integer studentId, int score);

    List<TaskSubmission> findByTaskIdAndStudentId(Integer taskId, Integer studentId);
}
