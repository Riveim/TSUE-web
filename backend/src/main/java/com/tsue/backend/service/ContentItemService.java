package com.tsue.backend.service;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentItemService {

    List<ContentItemDto> getByCategory(Category category);

    ContentItemDto getById(Long id);

    ContentItemDto create(Category category, CreateContentItemRequest request);

    ContentItemDto createWithFile(Category category, String title, String description, MultipartFile file);

    void delete(Long id);
}
