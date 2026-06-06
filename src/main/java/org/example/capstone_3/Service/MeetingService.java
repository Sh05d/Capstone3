package org.example.capstone_3.Service;

import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.Student;

public interface MeetingService {

    MeetingResponse createMeeting(MockInterview mockInterview, Student student, Mentor mentor);
}