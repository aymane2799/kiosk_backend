package com.example.kiosk.content;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.response.ContentDetailResponse;
import com.example.kiosk.content.response.ContentSummaryResponse;

import java.util.List;

public interface ContentService {
    List<ContentSummaryResponse> listCatalog(String userId);
    ContentDetailResponse getById(String id, String userId);
}
