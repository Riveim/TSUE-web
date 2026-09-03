package com.tsue.backend.edupage;

public class ClassNotFoundInScheduleException extends RuntimeException {

    public ClassNotFoundInScheduleException(String className) {
        super("Класс/группа не найдена в расписании: " + className);
    }
}
