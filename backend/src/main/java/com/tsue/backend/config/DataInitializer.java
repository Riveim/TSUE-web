package com.tsue.backend.config;

import com.tsue.backend.entity.Category;
import com.tsue.backend.entity.ContentItem;
import com.tsue.backend.repository.ContentItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Наполняет базу тестовыми данными при каждом старте приложения.
 * Удобно для разработки — база H2 in-memory, так что данные не сохраняются между перезапусками.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ContentItemRepository repository;

    public DataInitializer(ContentItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        repository.save(new ContentItem(Category.SUBJECTS, "Математика", "Преподаватель: Иванов И.И."));
        repository.save(new ContentItem(Category.SUBJECTS, "Физика", "Преподаватель: Петров П.П."));
        repository.save(new ContentItem(Category.SUBJECTS, "Программирование", "Преподаватель: Сидорова А.К."));

        repository.save(new ContentItem(Category.HOMEWORK, "Математика", "Решить задачи 12–20, стр. 45"));
        repository.save(new ContentItem(Category.HOMEWORK, "Физика", "Лабораторная работа №3"));

        repository.save(new ContentItem(Category.PRESENTATIONS, "История ТГЭУ", "Презентация к юбилею университета"));

        repository.save(new ContentItem(Category.NOTES, "Лекция 1: Введение", "Конспект по программированию"));

        repository.save(new ContentItem(Category.SCHEDULE, "Понедельник", "9:00 — Математика, ауд. 204"));
        repository.save(new ContentItem(Category.SCHEDULE, "Вторник", "10:30 — Физика, ауд. 118"));
    }
}
