package com.example.kiosk.favorite;

import com.example.kiosk.content.response.ContentSummaryResponse;

import java.util.List;

public interface FavoriteService {
    List<ContentSummaryResponse> listFavorites(String userId);
    ContentSummaryResponse addFavorite(String userId, String contentId);
    void removeFavorite(String userId, String contentId);
}
