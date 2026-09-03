package com.tsue.backend.edupage;

public class ScheduleDataNotLoadedException extends RuntimeException {

    public ScheduleDataNotLoadedException() {
        super("Данные расписания ещё не загружены. Загрузите файл через POST /api/timetable/upload");
    }
}
