package com.tsue.backend.service.impl;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;
import com.tsue.backend.entity.ContentItem;
import com.tsue.backend.exception.ItemNotFoundException;
import com.tsue.backend.repository.ContentItemRepository;
import com.tsue.backend.service.ContentItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentItemServiceImpl implements ContentItemService {

    private final ContentItemRepository repository;

    public ContentItemServiceImpl(ContentItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ContentItemDto> getByCategory(Category category) {
        return repository.findByCategory(category)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ContentItemDto create(Category category, CreateContentItemRequest request) {
        ContentItem item = new ContentItem(category, request.title(), request.description());
        ContentItem saved = repository.save(item);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private ContentItemDto toDto(ContentItem item) {
        return new ContentItemDto(item.getId(), item.getTitle(), item.getDescription());
    }
}
