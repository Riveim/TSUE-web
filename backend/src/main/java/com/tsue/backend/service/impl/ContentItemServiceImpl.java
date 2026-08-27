package com.tsue.backend.service.impl;

import com.tsue.backend.dto.ContentItemDto;
import com.tsue.backend.dto.CreateContentItemRequest;
import com.tsue.backend.entity.Category;
import com.tsue.backend.entity.ContentItem;
import com.tsue.backend.exception.ItemNotFoundException;
import com.tsue.backend.repository.ContentItemRepository;
import com.tsue.backend.service.ContentItemService;
import com.tsue.backend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ContentItemServiceImpl implements ContentItemService {

    private final ContentItemRepository repository;
    private final FileStorageService fileStorageService;

    public ContentItemServiceImpl(ContentItemRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<ContentItemDto> getByCategory(Category category) {
        return repository.findByCategory(category)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ContentItemDto getById(Long id) {
        ContentItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        return toDto(item);
    }

    @Override
    public ContentItemDto create(Category category, CreateContentItemRequest request) {
        ContentItem item = new ContentItem(category, request.title(), request.description());
        ContentItem saved = repository.save(item);
        return toDto(saved);
    }

    @Override
    public ContentItemDto createWithFile(Category category, String title, String description, MultipartFile file) {
        ContentItem item = new ContentItem(category, title, description);

        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.store(file);
            item.setOriginalFileName(file.getOriginalFilename());
            item.setStoredFileName(storedFileName);
        }

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
        String downloadUrl = item.getStoredFileName() != null
                ? "/api/files/" + item.getStoredFileName()
                : null;
        return new ContentItemDto(item.getId(), item.getTitle(), item.getDescription(), downloadUrl);
    }
}
