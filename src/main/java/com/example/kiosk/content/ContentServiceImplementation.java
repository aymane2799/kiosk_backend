package com.example.kiosk.content;

import com.example.kiosk.auth.auth.AuthService;
import com.example.kiosk.auth.auth.AuthServiceImplementation;
import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.response.ContentDetailResponse;
import com.example.kiosk.content.response.ContentSummaryResponse;
import com.example.kiosk.entitlement.EntitlementService;
import com.example.kiosk.entitlement.EntitlementServiceImplementation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ContentServiceImplementation implements ContentService{
    private final ContentRepository contentRepository;
    private final AuthServiceImplementation authService;
    private final EntitlementServiceImplementation entitlementService;

    public List<ContentSummaryResponse> listCatalog(String userId) {
        AppUser user = authService.requireUser(userId);

        return contentRepository.findAll().stream()
                .map(content -> ContentSummaryResponse.from(content, !entitlementService.hasAccess(user, content)))
                .toList();
    }

    public ContentDetailResponse getById(String id, String userId) {
        AppUser user = authService.requireUser(userId);

        Content content = contentRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        if(!entitlementService.hasAccess(user, content)) {
            throw new  ResponseStatusException(HttpStatus.FORBIDDEN, "INSUFFICIENT_TIER");
        }

        return ContentDetailResponse.from(content);
    }


}
