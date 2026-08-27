package com.tsue.backend.dto;

/**
 * То, что реально уходит на фронтенд в виде JSON.
 * downloadUrl заполняется только если к элементу прикреплён файл, иначе null.
 */
public record ContentItemDto(Long id, String title, String description, String downloadUrl) {
}
