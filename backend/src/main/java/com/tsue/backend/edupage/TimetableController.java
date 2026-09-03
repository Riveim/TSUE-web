package com.tsue.backend.edupage;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GET  /api/timetable/classes         — список всех классов/групп
 * GET  /api/timetable/{className}      — расписание конкретного класса, напр. /api/timetable/CYB1-26
 * POST /api/timetable/upload            — загрузить свежий JSON от EduPage (multipart, поле "file")
 */
@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final EduPageScheduleService service;

    public TimetableController(EduPageScheduleService service) {
        this.service = service;
    }

    @GetMapping("/classes")
    public List<String> listClasses() {
        return service.listClassNames();
    }

    @GetMapping("/{className}")
    public List<ScheduleEntryDto> getSchedule(@PathVariable String className) {
        return service.getScheduleForClass(className);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        service.updateSourceData(file.getBytes());
        return Map.of(
                "status", "ok",
                "message", "Данные расписания обновлены"
        );
    }
}
