package org.example.capstone_3.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(50) not null")
    private String fullName;

    @Column(unique = true, columnDefinition = "varchar(100) not null")
    private String email;

    @Column(columnDefinition = "varchar(255) not null")
    private String password;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;
}