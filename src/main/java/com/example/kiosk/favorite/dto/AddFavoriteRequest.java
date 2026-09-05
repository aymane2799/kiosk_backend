package com.example.kiosk.favorite.dto;

import jakarta.validation.constraints.NotBlank;

public record AddFavoriteRequest(
        @NotBlank String contentId
) {
}
