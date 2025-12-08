package com.lqm.transcript_service.models;

import java.util.UUID;

public class Transcript {
    private UUID id;
    private UUID classroomID;
    private UUID semesterID;
    private UUID subjectID;
    private UUID teacherID;
    private GradeStatusType gradeStatus;
}
