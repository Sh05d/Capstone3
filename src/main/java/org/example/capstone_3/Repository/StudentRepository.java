package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Student findStudentById(Integer id);

    Student findStudentByEmail(String email);

    Student findStudentByPhoneNumber(String phoneNumber);

    List<Student> findAllByOrderByXpDescFullNameAsc();

    long countByXpGreaterThan(Integer xp);

    @Query("select s from Student s join s.learningGroups g where g.id=?1")
    List<Student> findStudentsByGroupId(Integer learningGroupId);
}