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
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(80) not null")
    private String title;

    @Column(columnDefinition = "varchar(100) not null")
    private String targetRole;

    @Column(columnDefinition = "int not null")
    private Integer progressPercentage;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String skillGapSummary; // target role and student skills based

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RoadmapStep> roadmapSteps;
}