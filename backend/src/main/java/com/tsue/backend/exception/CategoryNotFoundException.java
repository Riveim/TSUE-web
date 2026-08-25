package com.tsue.backend.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String rawCategory) {
        super("Неизвестный раздел: " + rawCategory);
    }
}
