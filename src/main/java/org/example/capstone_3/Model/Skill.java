package org.example.capstone_3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, columnDefinition = "varchar(50) not null")
    private String name;

    @Column(columnDefinition = "varchar(50) not null")
    private String category;

    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    private Set<Student> students;

    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    private Set<JobAnalysis> jobAnalyses;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Challenge> challenges;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RoadmapStep> roadmapSteps;
}