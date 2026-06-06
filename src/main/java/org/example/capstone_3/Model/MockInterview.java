package org.example.capstone_3.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints =
        "(status='PENDING' or status='SCHEDULE' or status='COMPLETE' or status='REJECT' or status='CANCEL') " +
        "and (interview_mode='MENTOR' or interview_mode='AI')")
public class MockInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "interview_mode", columnDefinition = "varchar(20) not null")
    private String interviewMode; // MENTOR or AI

    @Column(columnDefinition = "varchar(80) not null")
    private String interviewType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "datetime")
    private LocalDateTime scheduledAt;

    @Column(columnDefinition = "int")
    private Integer durationMinutes;

    @Column(columnDefinition = "varchar(80) not null")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String questions;

    @Column(columnDefinition = "TEXT")
    private String studentAnswers;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "int")
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "varchar(50)")
    private String meetingProvider; // ZOOM or GOOGLE_MEET

    @Column(columnDefinition = "varchar(255)")
    private String externalMeetingId;

    @Column(columnDefinition = "boolean not null")
    private Boolean whatsappReminderSent;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @OneToOne(mappedBy = "mockInterview", cascade = CascadeType.ALL)
    @JsonIgnore
    private MockInterviewReport mockInterviewReport;
}