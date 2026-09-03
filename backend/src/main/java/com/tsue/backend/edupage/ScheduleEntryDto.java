package com.tsue.backend.edupage;

/**
 * Одна запись расписания — то, что уходит на фронтенд.
 * Соответствует итоговой цепочке:
 *   card.days (день недели) → card.period → lesson.subjectid/teacherids → card.classroomids
 */
public record ScheduleEntryDto(
        String day,
        int period,
        String time,
        String subject,
        String teachers,
        String room
) {
}
