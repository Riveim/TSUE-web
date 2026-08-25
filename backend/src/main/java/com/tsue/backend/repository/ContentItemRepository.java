package com.tsue.backend.repository;

import com.tsue.backend.entity.Category;
import com.tsue.backend.entity.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentItemRepository extends JpaRepository<ContentItem, Long> {

    List<ContentItem> findByCategory(Category category);
}
