package com.tsue.backend.controller;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;
import com.tsue.backend.exception.CategoryNotFoundException;
import com.tsue.backend.service.ContentItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{category}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentItemDto create(@PathVariable String category,
                                  @Valid @RequestBody CreateContentItemRequest request) {
        return service.create(parseCategory(category), request);
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
