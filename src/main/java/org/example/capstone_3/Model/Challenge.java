package org.example.capstone_3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "challenges")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "difficulty='EASY' or difficulty='MEDIUM' or difficulty='HARD'")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(80) not null")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "int not null")
    private Integer points;

    @Column(columnDefinition = "varchar(80) not null")
    private String difficulty;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<ChallengeAttempt> challengeAttempts;
}
