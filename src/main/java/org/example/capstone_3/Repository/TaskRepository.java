package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    Task findTaskById(Integer id);

    @Query("select t from Task t where t.learningGroup.id=?1")
    List<Task> findTasksByLearningGroupId(Integer learningGroupId);

    @Query("select t from Task t where t.learningGroup.id = ?1 and t.id not in (select ts.task.id from TaskSubmission ts where ts.student.id = ?2)")
    List<Task> findUnsubmittedTasksByGroupAndStudent(Integer groupId, Integer studentId);

    @Query("select t from Task t where t.learningGroup.id=?1 and t.deadline < CURRENT_TIMESTAMP")
    List<Task> groupOldTasks (Integer groupId);

    @Query("select t from Task t where t.learningGroup.id=?1 and t.deadline > CURRENT_TIMESTAMP")
    List<Task> groupAvailableTasks (Integer groupId);

    List<Task> findTasksByWhatsappReminderSentFalse();
}
