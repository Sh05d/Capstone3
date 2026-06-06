package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.LearningGroupDTOIN;
import org.example.capstone_3.DTO.OUT.LearningGroupDTOOUT;
import org.example.capstone_3.DTO.OUT.StudentSummaryDTOOut;
import org.example.capstone_3.DTO.OUT.TaskDTOOUT;
import org.example.capstone_3.Model.LearningGroup;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Model.Task;
import org.example.capstone_3.Repository.LearningGroupRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LearningGroupService {

    private final LearningGroupRepository learningGroupRepository;
    private final TaskService taskService;
    private final StudentRepository studentRepository;
    private final WhatsAppService whatsAppService;


    public List<LearningGroupDTOOUT> getAllLearningGroups() {

        List<LearningGroupDTOOUT> groupDTO = new ArrayList<>();

        for (LearningGroup learningGroup : learningGroupRepository.findAll()) {
            groupDTO.add(convertToDTO(learningGroup));
        }

        return groupDTO;
    }

    public LearningGroupDTOOUT getLearningGroupById(Integer id) {

        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(id);

        if (learningGroup == null) {
            throw new ApiException("Learning Group not found");
        }

        return convertToDTO(learningGroup);
    }

    public void createLearningGroup(Integer student_id, LearningGroupDTOIN dto) {

        Student student = studentRepository.findStudentById(student_id);
        if(student == null){
            throw new ApiException("Student not exist");
        }

        LearningGroup learningGroup = new LearningGroup();

        applyDto(learningGroup, dto);
        learningGroup.setCreatedAt(LocalDateTime.now());

        if (dto.getGroupType().equalsIgnoreCase("Private")) {
            learningGroup.setCode(generateUniqueCode());
        }

        learningGroupRepository.save(learningGroup);
        student.getLearningGroups().add(learningGroup);
        studentRepository.save(student);
    }

    public void updateLearningGroup(Integer id, LearningGroupDTOIN dto) {

        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(id);

        if (learningGroup == null) {
            throw new ApiException("Learning Group not found");
        }

        applyDto(learningGroup, dto);

        if (dto.getGroupType().equalsIgnoreCase("Public")) {
            learningGroup.setCode(null);
        } else if (dto.getGroupType().equalsIgnoreCase("Private") && learningGroup.getCode() == null) {
            learningGroup.setCode(generateUniqueCode());
        }

        learningGroupRepository.save(learningGroup);
    }

    public void deleteLearningGroup(Integer id) {

        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(id);

        if (learningGroup == null) {
            throw new ApiException("Learning Group not found");
        }

        learningGroupRepository.delete(learningGroup);
    }

    public void joinPrivateGroup(Integer student_id, String code) {
        Student student = findStudent(student_id);

        LearningGroup learningGroup = learningGroupRepository.findLearningGroupByCode(code);
        if (learningGroup == null) {
            throw new ApiException("Invalid code");
        }

        if (student.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("Student already in this group");
        }

        student.getLearningGroups().add(learningGroup);
        studentRepository.save(student);
    }

    public void joinPublicGroup(Integer student_id, Integer group_id) {
        Student student = findStudent(student_id);
        LearningGroup learningGroup = findLearningGroup(group_id);

        if (!learningGroup.getGroupType().equalsIgnoreCase("Public")) {
            throw new ApiException("This group is private, use the code to join");
        }

        if (student.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("Student already in this group");
        }

        student.getLearningGroups().add(learningGroup);
        studentRepository.save(student);
    }

    public void leaveGroup(Integer student_id, Integer group_id) {
        Student student = findStudent(student_id);
        LearningGroup learningGroup = findLearningGroup(group_id);

        if (!student.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("Student is not in this group");
        }

        student.getLearningGroups().remove(learningGroup);
        studentRepository.save(student);

    }

    public void inviteStudentToPrivateGroup(Integer inviter_id, Integer invited_student_id, Integer group_id) {

        Student inviter = findStudent(inviter_id);
        LearningGroup learningGroup = findLearningGroup(group_id);

        if (!inviter.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("You are not a member of this group");
        }

        if (!learningGroup.getGroupType().equalsIgnoreCase("Private")) {
            throw new ApiException("Group is public, no invite needed");
        }

        if (learningGroup.getCode() == null) {
            throw new ApiException("Group has no code");
        }

        Student invitedStudent = findStudent(invited_student_id);

        if (invitedStudent.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("Student is already in this group");
        }

        whatsAppService.sendInviteMessage(invitedStudent, learningGroup, inviter);
    }

    public List<StudentSummaryDTOOut> groupMembers(Integer learningGroupId){
        List<StudentSummaryDTOOut> members = new ArrayList<>();
        for(Student student: studentRepository.findStudentsByGroupId(learningGroupId)){
            members.add(convertToStudentSummaryDTOOut(student));
        }
        return members;
    }

    public List<LearningGroupDTOOUT> studentGroups(Integer studentId) {
        findStudent(studentId);

        List<LearningGroupDTOOUT> groups = new ArrayList<>();
        for (LearningGroup group : learningGroupRepository.studentGroups(studentId)) {
            groups.add(convertToDTO(group));
        }
        return groups;
    }

    public StudentSummaryDTOOut convertToStudentSummaryDTOOut(Student student){
        return new StudentSummaryDTOOut(student.getId(),student.getFullName());
    }

    private Student findStudent(Integer student_id) {
        Student student = studentRepository.findStudentById(student_id);
        if (student == null) {
            throw new ApiException("Student not found");
        }
        return student;
    }

    private LearningGroup findLearningGroup(Integer group_id) {
        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(group_id);
        if (learningGroup == null) {
            throw new ApiException("Learning group not found");
        }
        return learningGroup;
    }

    private void applyDto(LearningGroup learningGroup, LearningGroupDTOIN dto) {
        learningGroup.setName(dto.getName());
        learningGroup.setFocusArea(dto.getFocusArea());
        learningGroup.setDescription(dto.getDescription());
        learningGroup.setGroupType(dto.getGroupType());
    }

    public LearningGroupDTOOUT convertToDTO(LearningGroup learningGroup) {

        List<TaskDTOOUT> tasks = new ArrayList<>();

        if (learningGroup.getTasks() != null) {
            for (Task task : learningGroup.getTasks()) {
                tasks.add(taskService.convertToDTO(task));
            }
        }

        return new LearningGroupDTOOUT(
                learningGroup.getId(),
                learningGroup.getName(),
                learningGroup.getFocusArea(),
                learningGroup.getDescription(),
                learningGroup.getCreatedAt()
        );
    }

    private String generateUniqueCode() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = sb.toString();
        } while (learningGroupRepository.findLearningGroupByCode(code) != null);
        return code;
    }
}
