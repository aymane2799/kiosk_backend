package com.example.kiosk.favorite;

import com.example.kiosk.common.ApiPaths;
import com.example.kiosk.content.response.ContentSummaryResponse;
import com.example.kiosk.favorite.dto.AddFavoriteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/me/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteServiceImplementation favoriteService;

    @GetMapping
    public List<ContentSummaryResponse> list(Authentication authentication) {
        return favoriteService.listFavorites(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentSummaryResponse add(Authentication authentication, @Valid  @RequestBody AddFavoriteRequest request) {
        return favoriteService.addFavorite(authentication.getName(), request.contentId());
    }

    @DeleteMapping("/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable String contentId) {
        favoriteService.removeFavorite(authentication.getName(), contentId);
    }
}
