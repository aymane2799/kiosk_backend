package com.example.kiosk.favorite;

import com.example.kiosk.auth.auth.AuthService;
import com.example.kiosk.auth.auth.AuthServiceImplementation;
import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.Content;
import com.example.kiosk.content.ContentRepository;
import com.example.kiosk.content.response.ContentSummaryResponse;
import com.example.kiosk.entitlement.EntitlementService;
import com.example.kiosk.entitlement.EntitlementServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImplementation implements FavoriteService{

    private final AuthServiceImplementation authService;
    private final FavoriteRepository favoriteRepository;
    private final EntitlementServiceImplementation entitlementService;
    private final ContentRepository contentRepository;


    public List<ContentSummaryResponse> listFavorites(String userId) {
        AppUser user = authService.requireUser(userId);
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getContent)
                .map(content -> ContentSummaryResponse.from(content, !entitlementService.hasAccess(user, content)))
                .toList();
    }

    public ContentSummaryResponse addFavorite(String userId, String contentId) {
        AppUser user = authService.requireUser(userId);

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        Favorite favorite = favoriteRepository.findByUserIdAndContentId(userId, contentId)
                .orElseGet(()-> favoriteRepository.save(
                        Favorite.builder()
                                .user(user)
                                .content(content)
                                .build()));

        return ContentSummaryResponse.from(favorite.getContent(), !entitlementService.hasAccess(user, content));
    }

    public void removeFavorite(String userId, String contentId) {
        Favorite favorite = favoriteRepository.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite Content not found"));

         favoriteRepository.delete(favorite);
    }
}
