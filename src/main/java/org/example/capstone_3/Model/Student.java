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
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(50) not null")
    private String fullName;

    @Column(unique = true, columnDefinition = "varchar(13) not null")
    private String phoneNumber;

    @Column(unique = true, columnDefinition = "varchar(100) not null")
    private String email;

    @Column(columnDefinition = "varchar(255) not null")
    private String password;

    @Column(columnDefinition = "varchar(60) not null")
    private String major;

    @Column(columnDefinition = "varchar(80) not null")
    private String targetRole;

    @Column(columnDefinition = "int not null")
    private Integer yearsExperience;

    @Column(columnDefinition = "varchar(255)")
    private String linkedinUrl;

    @Column(columnDefinition = "varchar(255)")
    private String githubUrl;

    @Column(columnDefinition = "text")
    private String githubText;

    @Column(columnDefinition = "varchar(2048)")
    private String cvUrl;

    @Column(columnDefinition = "text")
    private String cvText;

    @Column(columnDefinition = "int not null")
    private Integer xp;

    @Column(columnDefinition = "int not null")
    private Integer readinessScore;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;

    @ManyToMany
    @JoinTable(
            name = "student_learning_groups",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "learning_group_id")
    )
    private Set<LearningGroup> learningGroups;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<TaskSubmission> taskSubmissions;

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private Set<Review> reviews;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<JobAnalysis> jobAnalyses;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Roadmap> roadmaps;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<ChallengeAttempt> challengeAttempts;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<MockInterview> mockInterviews;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<MockInterviewReport> mockInterviewReports;
}