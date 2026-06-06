package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap,Integer> {
    Roadmap findRoadmapById(Integer id);

    List<Roadmap> findRoadmapsByStudentId(Integer studentId);
}
