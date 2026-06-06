package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Integer> {

    Mentor findMentorById(Integer id);

    Mentor findMentorByEmail(String email);

    List<Mentor> findMentorsBySpecializationContainingIgnoreCase(String specialization);

    List<Mentor> findMentorsByJobTitleContainingIgnoreCase(String jobTitle);

    List<Mentor> findMentorsByAcceptedByAdmin(Boolean acceptedByAdmin);
}