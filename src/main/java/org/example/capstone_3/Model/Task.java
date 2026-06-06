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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Check(constraints = "difficulty='EASY' or difficulty='MEDIUM' or difficulty='HARD'")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "int not null")
    private Integer points;

    @Column(columnDefinition = "varchar(80) not null")
    private String difficulty;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime deadline;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "learning_group_id")
    private LearningGroup learningGroup;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<TaskSubmission> taskSubmissions;

    @Column(columnDefinition = "boolean default false")
    private Boolean whatsappReminderSent = false;
}
