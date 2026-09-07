package com.example.kiosk.content.response;

import com.example.kiosk.content.Content;
import com.example.kiosk.content.ContentRepository;
import com.example.kiosk.content.ContentTier;

import java.time.Instant;

public record ContentSummaryResponse(
        String id,
        String title,
        String excerpt,
        String category,
        ContentTier tier,
        Instant publishedAt,
        boolean locked,
        boolean favorite
) {
    public static ContentSummaryResponse from(Content content, boolean locked,boolean favorite) {
        return  new  ContentSummaryResponse(
                content.getId(),
                content.getTitle(),
                content.getExcerpt(),
                content.getCategory(),
                content.getTier(),
                content.getPublishedAt(),
                locked,
                favorite
        );
    }
}
