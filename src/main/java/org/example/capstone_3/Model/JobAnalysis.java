package org.example.capstone_3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(100)")
    private String jobTitle;

    @Column(columnDefinition = "text not null")
    private String jobDescription;

    @Column(name = "required_skills_text", columnDefinition = "text")
    private String strengths;

    @Column(columnDefinition = "text")
    private String missingSkillsText;

    @Column(columnDefinition = "int not null")
    private Integer matchScore;

    @Column(columnDefinition = "int")
    private Integer readinessScore;

    @Column(columnDefinition = "text not null")
    private String summary;

    @Column(name = "improvements", columnDefinition = "text not null")
    private String weaknesses;

    @Column(columnDefinition = "text")
    private String recommendations;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToMany
    @JoinTable(
            name = "job_analysis_skills",
            joinColumns = @JoinColumn(name = "job_analysis_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;

}