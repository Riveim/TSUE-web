package com.tsue.backend.dto;

/**
 * То, что реально уходит на фронтенд в виде JSON.
 * Соответствует ожиданиям script.js: item.title и item.description.
 */
public record ContentItemDto(Long id, String title, String description) {
}
