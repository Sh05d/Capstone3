package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.RoadmapStepDTOIN;
import org.example.capstone_3.DTO.OUT.CurrentRoadmapStepDTOOut;
import org.example.capstone_3.DTO.OUT.RoadmapStepDTOOUT;
import org.example.capstone_3.Model.Roadmap;
import org.example.capstone_3.Model.RoadmapStep;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Repository.RoadmapRepository;
import org.example.capstone_3.Repository.RoadmapStepRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapStepService {
    private final RoadmapStepRepository roadmapStepRepository;
    private final RoadmapRepository roadmapRepository;
    private final SkillRepository skillRepository;

    public List<RoadmapStepDTOOUT> getAllRoadmapSteps() {
        List<RoadmapStepDTOOUT> stepDTO = new ArrayList<>();

        for (RoadmapStep roadmapStep : roadmapStepRepository.findAll()) {
            stepDTO.add(convertToDTO(roadmapStep));
        }
        return stepDTO;
    }

    public RoadmapStepDTOOUT getRoadmapStepById(Integer id){
        RoadmapStep roadmapStep = findRoadmapStep(id);

        return convertToDTO(roadmapStep);
    }

    public void addRoadmapStep(Integer roadmap_id, RoadmapStepDTOIN dto) {
        Roadmap roadmap = findRoadmap(roadmap_id);

        Skill skill = findSkill(dto.getSkillId());

        RoadmapStep roadmapStep = new RoadmapStep();

        roadmapStep.setTitle(dto.getTitle());
        roadmapStep.setDescription(dto.getDescription());
        roadmapStep.setOrderNumber(dto.getOrderNumber());
        roadmapStep.setCompleted(false);
        roadmapStep.setCompletedAt(LocalDateTime.now());

        roadmapStep.setRoadmap(roadmap);
        roadmapStep.setSkill(skill);
        roadmapStepRepository.save(roadmapStep);
    }

    public void saveGeneratedStep(
            Roadmap roadmap,
            String title,
            String description,
            Integer orderNumber,
            Skill skill) {
        RoadmapStep roadmapStep = new RoadmapStep();
        roadmapStep.setTitle(title);
        roadmapStep.setDescription(description);
        roadmapStep.setOrderNumber(orderNumber);
        roadmapStep.setCompleted(false);
        roadmapStep.setCompletedAt(LocalDateTime.now());
        roadmapStep.setRoadmap(roadmap);
        roadmapStep.setSkill(skill);
        roadmapStepRepository.save(roadmapStep);
    }

    public void updateRoadmapStep(Integer id, RoadmapStepDTOIN dto){
        RoadmapStep roadmapStep = findRoadmapStep(id);

        Skill skill = findSkill(dto.getSkillId());

        roadmapStep.setTitle(dto.getTitle());
        roadmapStep.setDescription(dto.getDescription());
        roadmapStep.setOrderNumber(dto.getOrderNumber());
        roadmapStep.setSkill(skill);

        roadmapStepRepository.save(roadmapStep);
    }

    public void deleteRoadmapStep(Integer id){
        RoadmapStep roadmapStep = findRoadmapStep(id);

        roadmapStepRepository.delete(roadmapStep);
    }

    public void completeStep(Integer student_id, Integer roadmap_id, Integer step_id) {
        Roadmap roadmap = findRoadmap(roadmap_id);

        if (!roadmap.getStudent().getId().equals(student_id)) {
            throw new ApiException("Roadmap does not belong to this student");
        }

        RoadmapStep step = findRoadmapStep(step_id);

        if (!step.getRoadmap().getId().equals(roadmap_id)) {
            throw new ApiException("Step does not belong to this roadmap");
        }

        if (step.getCompleted()) {
            throw new ApiException("Step already completed");
        }

        if (step.getOrderNumber() > 1) {
            RoadmapStep previousStep = roadmapStepRepository.findByRoadmapIdAndOrderNumber(roadmap_id, step.getOrderNumber() - 1);
            if (previousStep != null && !previousStep.getCompleted()) {
                throw new ApiException("You must complete step " + previousStep.getId() + ": "+ previousStep.getTitle() + " first");
            }
        }

        step.setCompleted(true);
        step.setCompletedAt(LocalDateTime.now());
        roadmapStepRepository.save(step);

        Roadmap updatedRoadmap = findRoadmap(roadmap_id);
        updateProgress(updatedRoadmap);
    }

    private void updateProgress(Roadmap roadmap) {
        int completedCount = 0;
        for (RoadmapStep s : roadmap.getRoadmapSteps()) {
            if (s.getCompleted()) {
                completedCount++;
            }
        }
        int progress = (int) ((double) completedCount / roadmap.getRoadmapSteps().size() * 100);

        roadmap.setProgressPercentage(progress);
        roadmapRepository.save(roadmap);
    }

    public RoadmapStepDTOOUT getNextStep(Integer student_id, Integer roadmap_id) {
        CurrentRoadmapStepDTOOut current = getCurrentRoadmapStep(student_id, roadmap_id);
        if (current.getCurrentStep() == null) {
            throw new ApiException("All steps are completed");
        }
        return current.getCurrentStep();
    }

    public CurrentRoadmapStepDTOOut getCurrentRoadmapStep(Integer studentId, Integer roadmapId) {
        Roadmap roadmap = findRoadmap(roadmapId);

        if (!roadmap.getStudent().getId().equals(studentId)) {
            throw new ApiException("Roadmap does not belong to this student");
        }

        List<RoadmapStep> allSteps = roadmapStepRepository.findByRoadmapIdOrderByOrderNumber(roadmapId);
        int completedCount = 0;
        RoadmapStep current = null;

        for (RoadmapStep step : allSteps) {
            if (Boolean.TRUE.equals(step.getCompleted())) {
                completedCount++;
            } else if (current == null) {
                current = step;
            }
        }

        boolean allCompleted = !allSteps.isEmpty() && current == null;

        return new CurrentRoadmapStepDTOOut(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getTargetRole(),
                roadmap.getProgressPercentage(),
                current != null ? convertToDTO(current) : null,
                allCompleted,
                allSteps.size(),
                completedCount
        );
    }

    private Roadmap findRoadmap(Integer roadmap_id) {
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmap_id);
        if (roadmap == null) {
            throw new ApiException("Roadmap not found");
        }
        return roadmap;
    }

    private RoadmapStep findRoadmapStep(Integer step_id) {
        RoadmapStep step = roadmapStepRepository.findRoadmapStepById(step_id);
        if (step == null) {
            throw new ApiException("Roadmap step not found");
        }
        return step;
    }

    private Skill findSkill(Integer skill_id) {
        Skill skill = skillRepository.findSkillById(skill_id);
        if (skill == null) {
            throw new ApiException("Skill not found");
        }
        return skill;
    }

    public RoadmapStepDTOOUT convertToDTO(RoadmapStep roadmapStep) {

        String skillName = null;

        if (roadmapStep.getSkill() != null) {
            skillName = roadmapStep.getSkill().getName();
        }

        return new RoadmapStepDTOOUT(
                roadmapStep.getId(),
                roadmapStep.getTitle(),
                roadmapStep.getDescription(),
                roadmapStep.getOrderNumber(),
                roadmapStep.getCompleted(),
                skillName,
                roadmapStep.getCompletedAt()
        );
    }
}
