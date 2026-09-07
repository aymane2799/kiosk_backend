package com.example.kiosk.content;

import com.example.kiosk.auth.auth.AuthService;
import com.example.kiosk.auth.auth.AuthServiceImplementation;
import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.response.ContentDetailResponse;
import com.example.kiosk.content.response.ContentSummaryResponse;
import com.example.kiosk.entitlement.EntitlementServiceImplementation;
import com.example.kiosk.favorite.FavoriteRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ContentServiceImplementation implements ContentService{
    private final ContentRepository contentRepository;
    private final AuthServiceImplementation authService;
    private final EntitlementServiceImplementation entitlementService;
    private final FavoriteRepository favoriteRepository;

    public List<ContentSummaryResponse> listCatalog(String userId) {
        AppUser user = authService.requireUser(userId);

        Set<String> favoriteIds = favoriteRepository.findContentIdsByUserId(userId);
        boolean isPremium = entitlementService.hasActivePremium(user);

        return contentRepository.findAll().stream()
                .map(content -> ContentSummaryResponse.from(
                        content,
                        isPremium,
                        favoriteIds.contains(content.getId())
                        ))
                .toList();
    }

    public ContentDetailResponse getById(String id, String userId) {
        AppUser user = authService.requireUser(userId);

        Content content = contentRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        if(!entitlementService.hasAccess(user, content)) {
            throw new  ResponseStatusException(HttpStatus.FORBIDDEN, "INSUFFICIENT_TIER");
        }

        boolean isFavorite = favoriteRepository.existsByUserIdAndContentId(userId, id);

        return ContentDetailResponse.from(content, isFavorite);
    }


}
