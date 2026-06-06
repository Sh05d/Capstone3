package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.AdminDTOIn;
import org.example.capstone_3.DTO.IN.SkillCategoryDTOIn;
import org.example.capstone_3.DTO.OUT.AdminDTOOut;
import org.example.capstone_3.DTO.OUT.MentorDTOOut;
import org.example.capstone_3.DTO.OUT.SkillCategoryGenerationDTOOut;
import org.example.capstone_3.DTO.OUT.SkillDTOOut;
import org.example.capstone_3.Model.Admin;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Repository.AdminRepository;
import org.example.capstone_3.Repository.MentorRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Pattern SKILLS_ARRAY_FIELD_PATTERN = Pattern.compile(
            "\"skills\"\\s*:\\s*\\[(.*?)\\]",
            Pattern.DOTALL);
    private static final Pattern QUOTED_STRING_PATTERN =
            Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    private final AdminRepository adminRepository;
    private final MentorRepository mentorRepository;
    private final MentorService mentorService;
    private final SkillRepository skillRepository;
    private final AiService aiService;

    public void create(AdminDTOIn dto) {

        if (adminRepository.findAdminByEmail(dto.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }

        Admin admin = new Admin();

        applyDto(admin, dto);
        admin.setCreatedAt(LocalDateTime.now());

        adminRepository.save(admin);
    }

    public AdminDTOOut getById(Integer id) {

        Admin admin = adminRepository.findAdminById(id);

        if (admin == null) {
            throw new ApiException("Admin with id " + id + " not found");
        }

        return toDtoOut(admin);
    }

    public List<AdminDTOOut> getAll() {

        List<Admin> admins = adminRepository.findAll();

        List<AdminDTOOut> adminDTOOuts = new ArrayList<>();

        for (Admin admin : admins) {
            adminDTOOuts.add(toDtoOut(admin));
        }

        return adminDTOOuts;
    }

    public void update(Integer id, AdminDTOIn dto) {

        Admin admin = adminRepository.findAdminById(id);

        if (admin == null) {
            throw new ApiException("Admin with id " + id + " not found");
        }

        Admin emailOwner = adminRepository.findAdminByEmail(dto.getEmail());

        if (emailOwner != null && !emailOwner.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }

        applyDto(admin, dto);

        adminRepository.save(admin);
    }

    public void delete(Integer id) {

        Admin admin = adminRepository.findAdminById(id);

        if (admin == null) {
            throw new ApiException("Admin with id " + id + " not found");
        }

        adminRepository.delete(admin);
    }

    public List<MentorDTOOut> getAllMentors(Integer adminId) {
        requireAdmin(adminId);
        return mentorService.getAllMentors();
    }

    public List<MentorDTOOut> getPendingMentors(Integer adminId) {
        requireAdmin(adminId);
        return mentorService.getPendingMentors();
    }

    public MentorDTOOut getMentorById(Integer adminId, Integer mentorId) {
        requireAdmin(adminId);
        return mentorService.getById(mentorId);
    }

    public void approveMentor(Integer adminId, Integer mentorId) {
        requireAdmin(adminId);

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        if (Boolean.TRUE.equals(mentor.getAcceptedByAdmin())) {
            throw new ApiException("Mentor with id " + mentorId + " is already approved");
        }

        mentor.setAcceptedByAdmin(true);
        mentorRepository.save(mentor);
    }

    public void unapproveMentor(Integer adminId, Integer mentorId) {
        requireAdmin(adminId);

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        if (!Boolean.TRUE.equals(mentor.getAcceptedByAdmin())) {
            throw new ApiException("Mentor with id " + mentorId + " is not approved");
        }

        mentor.setAcceptedByAdmin(false);
        mentorRepository.save(mentor);
    }

    @Transactional
    public SkillCategoryGenerationDTOOut generateSkillsFromCategory(Integer adminId, SkillCategoryDTOIn dto) {
        Admin admin = adminRepository.findAdminById(adminId);
        if (admin == null) {
            throw new ApiException("Admin with id " + adminId + " not found");
        }

        String category = dto.getCategory().trim();
        List<String> skillNames = fetchSkillNamesFromAi(category);
        if (skillNames.isEmpty()) {
            throw new ApiException("AI did not return any skills for category: " + category);
        }

        List<SkillDTOOut> savedSkills = new ArrayList<>();
        List<SkillDTOOut> existingSkills = new ArrayList<>();

        for (String rawName : skillNames) {
            String name = rawName.trim();
            if (name.isEmpty() || name.length() > 50) {
                continue;
            }

            Skill existing = skillRepository.findSkillByName(name);
            if (existing != null) {
                existingSkills.add(toSkillDtoOut(existing));
                continue;
            }

            Skill skill = new Skill();
            skill.setName(name);
            skill.setCategory(category);
            Skill saved = skillRepository.save(skill);
            savedSkills.add(toSkillDtoOut(saved));
        }

        if (savedSkills.isEmpty() && existingSkills.isEmpty()) {
            throw new ApiException("No valid skill names were returned for category: " + category);
        }

        return new SkillCategoryGenerationDTOOut(category, savedSkills, existingSkills);
    }

    private List<String> fetchSkillNamesFromAi(String category) {
        String json = aiService.ask(buildSkillsByCategoryPrompt(category));
        return parseSkillNamesFromJson(json);
    }

    private String buildSkillsByCategoryPrompt(String category) {
        return """
                You are a career skills catalog assistant for a professional development platform.
                Generate a list of distinct, real-world skill names that belong to the category below.
                
                Respond with JSON only using this exact shape:
                {
                  "skills": ["Skill One", "Skill Two"]
                }
                
                Rules:
                - Return between 8 and 20 skill names.
                - Each name must be 2 to 50 characters.
                - Use professional, commonly recognized skill names (not long sentences).
                - Do not repeat the same skill name.
                - Category for every skill: %s
                
                Category: %s
                """.formatted(category, category);
    }

    private List<String> parseSkillNamesFromJson(String json) {
        Matcher arrayMatcher = SKILLS_ARRAY_FIELD_PATTERN.matcher(json);
        if (!arrayMatcher.find()) {
            throw new AiException("AI response did not contain a skills array.");
        }

        Set<String> uniqueNames = new LinkedHashSet<>();
        Matcher stringMatcher = QUOTED_STRING_PATTERN.matcher(arrayMatcher.group(1));
        while (stringMatcher.find()) {
            String value = unescapeJsonString(stringMatcher.group(1));
            if (!value.isBlank()) {
                uniqueNames.add(value);
            }
        }
        return new ArrayList<>(uniqueNames);
    }

    private String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    private SkillDTOOut toSkillDtoOut(Skill skill) {
        return new SkillDTOOut(skill.getId(), skill.getName(), skill.getCategory());
    }

    private void applyDto(Admin admin, AdminDTOIn dto) {
        admin.setFullName(dto.getFullName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
    }

    private AdminDTOOut toDtoOut(Admin admin) {
        return new AdminDTOOut(
                admin.getId(),
                admin.getFullName(),
                admin.getEmail()
        );
    }

    private void requireAdmin(Integer adminId) {
        if (adminRepository.findAdminById(adminId) == null) {
            throw new ApiException("Admin with id " + adminId + " not found");
        }
    }
}
