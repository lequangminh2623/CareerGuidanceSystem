package com.lqm.academic_service.services;

import com.lqm.academic_service.events.*;

public interface AcademicEventPublisher {
    void publishScoreSync(ScoreSyncEvent event);
    void publishStudentsRemoved(StudentsRemovedEvent event);
    void publishClassroomDeleted(ClassroomDeletedEvent event);
    void publishChatGroupCreate(ChatGroupCreateEvent event);
    void publishChatGroupUpdateTeacher(ChatGroupUpdateTeacherEvent event);
    void publishChatGroupDelete(ChatGroupDeleteEvent event);
}
