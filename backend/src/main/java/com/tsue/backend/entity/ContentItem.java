package com.tsue.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "content_items")
public class ContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    protected ContentItem() {
        // требуется JPA
    }

    public ContentItem(Category category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
