package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.MentorDTOIn;
import org.example.capstone_3.DTO.OUT.MentorDTOOut;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Repository.MentorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;

    public void create(MentorDTOIn dto) {

        if (mentorRepository.findMentorByEmail(dto.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }

        Mentor mentor = new Mentor();

        applyDto(mentor, dto);

        mentor.setRating(0.0);
        mentor.setAcceptedByAdmin(false);
        mentor.setCreatedAt(LocalDateTime.now());

        mentorRepository.save(mentor);
    }

    public MentorDTOOut getApprovedById(Integer id) {
        Mentor mentor = mentorRepository.findMentorById(id);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + id + " not found");
        }
        if (!Boolean.TRUE.equals(mentor.getAcceptedByAdmin())) {
            throw new ApiException("Mentor with id " + id + " is not available");
        }
        return toDtoOut(mentor);
    }

    public MentorDTOOut getById(Integer id) {
        Mentor mentor = mentorRepository.findMentorById(id);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + id + " not found");
        }
        return toDtoOut(mentor);
    }

    public List<MentorDTOOut> getApprovedMentors() {
        return mapMentors(mentorRepository.findMentorsByAcceptedByAdmin(true));
    }

    public List<MentorDTOOut> getPendingMentors() {
        return mapMentors(mentorRepository.findMentorsByAcceptedByAdmin(false));
    }

    public List<MentorDTOOut> getAllMentors() {
        return mapMentors(mentorRepository.findAll());
    }

    private List<MentorDTOOut> mapMentors(List<Mentor> mentors) {
        List<MentorDTOOut> mentorDTOOuts = new ArrayList<>();
        for (Mentor mentor : mentors) {
            mentorDTOOuts.add(toDtoOut(mentor));
        }
        return mentorDTOOuts;
    }

    public void update(Integer id, MentorDTOIn dto) {

        Mentor mentor = mentorRepository.findMentorById(id);

        if (mentor == null) {
            throw new ApiException("Mentor with id " + id + " not found");
        }

        Mentor emailOwner = mentorRepository.findMentorByEmail(dto.getEmail());

        if (emailOwner != null && !emailOwner.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }

        applyDto(mentor, dto);

        mentorRepository.save(mentor);
    }

    public void delete(Integer id) {

        Mentor mentor = mentorRepository.findMentorById(id);

        if (mentor == null) {
            throw new ApiException("Mentor with id " + id + " not found");
        }

        mentorRepository.delete(mentor);
    }

    private void applyDto(Mentor mentor, MentorDTOIn dto) {
        mentor.setFullName(dto.getFullName());
        mentor.setPhoneNumber(dto.getPhoneNumber());
        mentor.setEmail(dto.getEmail());
        mentor.setPassword(dto.getPassword());
        mentor.setJobTitle(dto.getJobTitle());
        mentor.setCompany(dto.getCompany());
        mentor.setSpecialization(dto.getSpecialization());
        mentor.setYearsExperience(dto.getYearsExperience());
        mentor.setBio(dto.getBio());
        mentor.setSessionPrice(dto.getSessionPrice());
    }

    private MentorDTOOut toDtoOut(Mentor mentor) {
        return new MentorDTOOut(
                mentor.getId(),
                mentor.getFullName(),
                mentor.getPhoneNumber(),
                mentor.getEmail(),
                mentor.getJobTitle(),
                mentor.getCompany(),
                mentor.getSpecialization(),
                mentor.getYearsExperience(),
                mentor.getBio(),
                mentor.getSessionPrice(),
                mentor.getRating(),
                mentor.getAcceptedByAdmin()
        );
    }
}