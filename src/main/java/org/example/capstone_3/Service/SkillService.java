package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.SkillDTOIn;
import org.example.capstone_3.DTO.OUT.SkillDTOOut;
import org.example.capstone_3.Model.Admin;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Repository.AdminRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final AdminRepository adminRepository;

    public void create(Integer adminId, SkillDTOIn dto) {

        Admin admin = adminRepository.findAdminById(adminId);

        if (admin == null) {
            throw new ApiException("Admin with id " + adminId + " not found");
        }

        if (skillRepository.findSkillByName(dto.getName()) != null) {
            throw new ApiException("Skill already exists");
        }

        Skill skill = new Skill();

        applyDto(skill, dto);

        skillRepository.save(skill);
    }

    public SkillDTOOut getById(Integer id) {

        Skill skill = skillRepository.findSkillById(id);

        if (skill == null) {
            throw new ApiException("Skill with id " + id + " not found");
        }

        return toDtoOut(skill);
    }

    public List<SkillDTOOut> getAll() {

        List<Skill> skills = skillRepository.findAll();

        List<SkillDTOOut> skillDTOOuts = new ArrayList<>();

        for (Skill skill : skills) {
            skillDTOOuts.add(toDtoOut(skill));
        }

        return skillDTOOuts;
    }

    public void update(Integer adminId, Integer skillId, SkillDTOIn dto) {

        Admin admin = adminRepository.findAdminById(adminId);

        if (admin == null) {
            throw new ApiException("Admin with id " + adminId + " not found");
        }

        Skill skill = skillRepository.findSkillById(skillId);

        if (skill == null) {
            throw new ApiException("Skill with id " + skillId + " not found");
        }

        Skill nameOwner = skillRepository.findSkillByName(dto.getName());

        if (nameOwner != null && !nameOwner.getId().equals(skillId)) {
            throw new ApiException("Skill already exists");
        }

        applyDto(skill, dto);
        skillRepository.save(skill);
    }

    public void delete(Integer adminId, Integer skillId) {

        Admin admin = adminRepository.findAdminById(adminId);

        if (admin == null) {
            throw new ApiException("Admin with id " + adminId + " not found");
        }

        Skill skill = skillRepository.findSkillById(skillId);

        if (skill == null) {
            throw new ApiException("Skill with id " + skillId + " not found");
        }

        if (skill.getStudents() != null && !skill.getStudents().isEmpty()) {
            throw new ApiException("Cannot delete skill because it is assigned to students");
        }

        if (skill.getJobAnalyses() != null && !skill.getJobAnalyses().isEmpty()) {
            throw new ApiException("Cannot delete skill because it is used in job analyses");
        }

        skillRepository.delete(skill);
    }

    private void applyDto(Skill skill, SkillDTOIn dto) {
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
    }

    private SkillDTOOut toDtoOut(Skill skill) {
        return new SkillDTOOut(
                skill.getId(),
                skill.getName(),
                skill.getCategory()
        );
    }
}