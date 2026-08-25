package com.tsue.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * То, что приходит от клиента при создании нового элемента (POST /api/{category}).
 */
public record CreateContentItemRequest(
        @NotBlank(message = "title обязателен") String title,
        String description
) {
}
