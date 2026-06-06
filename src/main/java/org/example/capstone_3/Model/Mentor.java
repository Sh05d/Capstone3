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
public class Mentor {

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

    @Column(columnDefinition = "varchar(80) not null")
    private String jobTitle;

    @Column(columnDefinition = "varchar(80) not null")
    private String company;

    @Column(columnDefinition = "varchar(80) not null")
    private String specialization;

    @Column(columnDefinition = "int not null")
    private Integer yearsExperience;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(columnDefinition = "double not null")
    private Double sessionPrice;

    @Column(columnDefinition = "double not null")
    private Double rating;

    @Column(columnDefinition = "boolean not null")
    private Boolean acceptedByAdmin;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "mentor")
    @JsonIgnore
    private Set<Review> reviews;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<MockInterview> mockInterviews;
}