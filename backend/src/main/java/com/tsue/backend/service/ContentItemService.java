package com.tsue.backend.service;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;

import java.util.List;

public interface ContentItemService {

    List<ContentItemDto> getByCategory(Category category);

    ContentItemDto create(Category category, CreateContentItemRequest request);

    void delete(Long id);
}
