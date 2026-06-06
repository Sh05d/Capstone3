package org.example.capstone_3.Service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeetingResponse {

    private String meetingId;

    private String joinUrl;

    private String provider;
}