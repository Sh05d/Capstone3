package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Integer> {
    RoadmapStep findRoadmapStepById(Integer id);

    @Query("select r from RoadmapStep r where r.roadmap.id=?1 and r.orderNumber=?2")
    RoadmapStep findByRoadmapIdAndOrderNumber(Integer roadmapId,Integer orderNumber);

    @Query("select r from RoadmapStep r where r.roadmap.id=?1 order by r.orderNumber")
    List<RoadmapStep> findByRoadmapIdOrderByOrderNumber(Integer roadmapId);

}
