package com.example.kiosk.content.response;

import com.example.kiosk.content.Content;
import com.example.kiosk.content.ContentTier;

import java.time.Instant;

public record ContentDetailResponse(
        String id,
        String title,
        String excerpt,
        String body,
        String category,
        ContentTier tier,
        Instant publishedAt,
        boolean favorite
) {
    public static ContentDetailResponse from(Content content, boolean favorite) {
        return new ContentDetailResponse(
                content.getId(),
                content.getTitle(),
                content.getExcerpt(),
                content.getBody(),
                content.getCategory(),
                content.getTier(),
                content.getPublishedAt(),
                favorite
        );
    }
}
