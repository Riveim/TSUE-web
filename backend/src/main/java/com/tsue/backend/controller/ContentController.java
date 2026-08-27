package com.tsue.backend.controller;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;
import com.tsue.backend.exception.CategoryNotFoundException;
import com.tsue.backend.service.ContentItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Обрабатывает все разделы фронтенда через один контроллер:
 * GET  /api/homework
 * GET  /api/presentations
 * GET  /api/notes
 * GET  /api/subjects
 * GET  /api/schedule
 * (и POST/DELETE для тех же путей)
 *
 * Значение {category} в URL должно совпадать с data-endpoint в index.html.
 */
@RestController
@RequestMapping("/api")
public class ContentController {

    private final ContentItemService service;

    public ContentController(ContentItemService service) {
        this.service = service;
    }

    @GetMapping("/{category}")
    public List<ContentItemDto> getByCategory(@PathVariable String category) {
        return service.getByCategory(parseCategory(category));
    }

    @GetMapping("/{category}/{id}")
    public ContentItemDto getOne(@PathVariable String category, @PathVariable Long id) {
        // категория в пути проверяется для консистентности URL,
        // сам элемент ищется просто по id
        parseCategory(category);
        return service.getById(id);
    }

    @PostMapping("/{category}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentItemDto create(@PathVariable String category,
                                  @Valid @RequestBody CreateContentItemRequest request) {
        return service.create(parseCategory(category), request);
    }

    /**
     * Загрузка файла вместе с элементом.
     * Отправляется как multipart/form-data с полями: title, description (необязательно), file.
     */
    @PostMapping(value = "/{category}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContentItemDto uploadFile(@PathVariable String category,
                                      @RequestParam("title") String title,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam("file") MultipartFile file) {
        return service.createWithFile(parseCategory(category), title, description, file);
    }

    @DeleteMapping("/{category}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String category, @PathVariable Long id) {
        // category в пути не используется для удаления напрямую (id уникален),
        // но остаётся для консистентности REST-путей и на будущее (проверки прав и т.п.)
        parseCategory(category);
        service.delete(id);
    }

    private Category parseCategory(String raw) {
        try {
            return Category.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CategoryNotFoundException(raw);
        }
    }
}
