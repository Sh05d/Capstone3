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
@Check(constraints = "group_type='PUBLIC' or group_type='PRIVATE'")
public class LearningGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(80) not null")
    private String name;

    @Column(columnDefinition = "varchar(80) not null")
    private String focusArea;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "varchar(10) not null")
    private String groupType;

    @Column(columnDefinition = "varchar(6)")
    private String code;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "learningGroups")
    @JsonIgnore
    private Set<Student> students;

    @OneToMany(mappedBy = "learningGroup", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Task> tasks;
}