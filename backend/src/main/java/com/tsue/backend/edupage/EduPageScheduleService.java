package com.tsue.backend.edupage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Разбирает JSON-ответ EduPage-эндпоинта regularttGetData в удобные таблицы
 * и строит из них расписание для конкретного класса/группы.
 * <p>
 * Структура ответа: {"r": {"dbiAccessorRes": {"tables": [ {id, data_rows: [...]}, ... ]}}}
 * Нужные нам таблицы: subjects, teachers, classes, classrooms, lessons, cards, periods.
 * <p>
 * Логика связей:
 *   cards.lessonid   → lessons.id
 *   lessons.subjectid → subjects.name
 *   lessons.teacherids → teachers.name
 *   lessons.classids   → classes.name   (фильтр по нужному классу)
 *   cards.period        → periods.starttime/endtime
 *   cards.days           → битовая маска дней недели, напр. "100000" = только понедельник
 *   cards.classroomids   → classrooms.name
 */
@Service
public class EduPageScheduleService {

    private static final String[] DAY_NAMES = {
            "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"
    };

    private final ObjectMapper objectMapper;
    private final Path dataFilePath;

    // tableName -> (rowId -> row). volatile, т.к. может обновляться через upload из другого потока запроса
    private volatile Map<String, Map<String, JsonNode>> tables;

    public EduPageScheduleService(ObjectMapper objectMapper,
                                   @Value("${edupage.data-file}") String dataFile) {
        this.objectMapper = objectMapper;
        this.dataFilePath = Paths.get(dataFile);
        loadIfFileExists();
    }

    private void loadIfFileExists() {
        System.out.println("Ищу файл расписания по пути: " + dataFilePath.toAbsolutePath());
        System.out.println("Файл существует: " + Files.exists(dataFilePath));

        if (Files.exists(dataFilePath)) {
            try {
                JsonNode root = objectMapper.readTree(dataFilePath.toFile());
                this.tables = parseTables(root);
                System.out.println("Расписание успешно загружено, таблиц: " + tables.size());
            } catch (IOException e) {
                throw new RuntimeException("Не удалось прочитать файл расписания: " + dataFilePath, e);
            }
        }
    }

    /** Сохраняет новый JSON на диск и сразу перечитывает его в память. */
    public void updateSourceData(byte[] jsonBytes) {
        try {
            if (dataFilePath.getParent() != null) {
                Files.createDirectories(dataFilePath.getParent());
            }
            Files.write(dataFilePath, jsonBytes);

            JsonNode root = objectMapper.readTree(jsonBytes);
            this.tables = parseTables(root);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить/разобрать файл расписания", e);
        }
    }

    /** Список всех известных классов/групп — удобно для выпадающего списка на фронте. */
    public List<String> listClassNames() {
        ensureLoaded();
        List<String> names = new ArrayList<>();
        for (JsonNode c : tables.get("classes").values()) {
            names.add(c.path("name").asText());
        }
        names.sort(String::compareTo);
        return names;
    }

    /** Главный метод: строит расписание для класса/группы по его имени. */
    public List<ScheduleEntryDto> getScheduleForClass(String className) {
        ensureLoaded();

        Map<String, JsonNode> subjects = tables.get("subjects");
        Map<String, JsonNode> teachers = tables.get("teachers");
        Map<String, JsonNode> classes = tables.get("classes");
        Map<String, JsonNode> classrooms = tables.get("classrooms");
        Map<String, JsonNode> lessons = tables.get("lessons");
        Map<String, JsonNode> cards = tables.get("cards");
        Map<String, JsonNode> periods = tables.get("periods");

        String classId = findClassId(classes, className);
        if (classId == null) {
            throw new ClassNotFoundInScheduleException(className);
        }

        List<ScheduleEntryDto> result = new ArrayList<>();

        for (JsonNode card : cards.values()) {
            String lessonId = card.path("lessonid").asText();
            JsonNode lesson = lessons.get(lessonId);
            if (lesson == null || !lessonBelongsToClass(lesson, classId)) {
                continue;
            }

            JsonNode subjectNode = subjects.get(lesson.path("subjectid").asText());
            String subjectName = subjectNode != null ? subjectNode.path("name").asText("?") : "?";

            String teacherNames = joinNames(lesson.path("teacherids"), teachers);
            String roomNames = joinNames(card.path("classroomids"), classrooms);

            JsonNode period = periods.get(card.path("period").asText());
            String time = period != null
                    ? period.path("starttime").asText("?") + "-" + period.path("endtime").asText("?")
                    : "?";
            int periodNumber = card.path("period").asInt();

            String daysMask = card.path("days").asText();
            for (int dayIdx = 0; dayIdx < daysMask.length() && dayIdx < DAY_NAMES.length; dayIdx++) {
                if (daysMask.charAt(dayIdx) == '1') {
                    result.add(new ScheduleEntryDto(
                            DAY_NAMES[dayIdx],
                            periodNumber,
                            time,
                            subjectName,
                            teacherNames.isEmpty() ? "—" : teacherNames,
                            roomNames.isEmpty() ? "—" : roomNames
                    ));
                }
            }
        }

        result.sort(Comparator
                .comparingInt((ScheduleEntryDto e) -> dayIndex(e.day()))
                .thenComparingInt(ScheduleEntryDto::period));

        return result;
    }

    private boolean lessonBelongsToClass(JsonNode lesson, String classId) {
        for (JsonNode cid : lesson.path("classids")) {
            if (cid.asText().equals(classId)) {
                return true;
            }
        }
        return false;
    }

    private String joinNames(JsonNode idsArray, Map<String, JsonNode> lookupTable) {
        List<String> names = new ArrayList<>();
        for (JsonNode idNode : idsArray) {
            String id = idNode.asText();
            JsonNode row = lookupTable.get(id);
            names.add(row != null ? row.path("name").asText(id) : id);
        }
        return String.join(", ", names);
    }

    private String findClassId(Map<String, JsonNode> classes, String className) {
        for (Map.Entry<String, JsonNode> entry : classes.entrySet()) {
            JsonNode c = entry.getValue();
            if (className.equalsIgnoreCase(c.path("name").asText())
                    || className.equalsIgnoreCase(c.path("short").asText())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private int dayIndex(String dayName) {
        for (int i = 0; i < DAY_NAMES.length; i++) {
            if (DAY_NAMES[i].equals(dayName)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    private Map<String, Map<String, JsonNode>> parseTables(JsonNode root) {
        Map<String, Map<String, JsonNode>> result = new HashMap<>();

        JsonNode tablesArray = root.path("r").path("dbiAccessorRes").path("tables");
        for (JsonNode table : tablesArray) {
            String tableId = table.path("id").asText();
            Map<String, JsonNode> rows = new HashMap<>();
            for (JsonNode row : table.path("data_rows")) {
                rows.put(row.path("id").asText(), row);
            }
            result.put(tableId, rows);
        }

        return result;
    }

    private void ensureLoaded() {
        if (tables == null) {
            throw new ScheduleDataNotLoadedException();
        }
    }
}
