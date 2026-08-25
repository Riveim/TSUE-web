package com.tsue.backend.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Элемент с id=" + id + " не найден");
    }
}
