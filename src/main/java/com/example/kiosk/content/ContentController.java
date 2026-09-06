package com.example.kiosk.content;

import com.example.kiosk.common.ApiPaths;
import com.example.kiosk.content.response.ContentDetailResponse;
import com.example.kiosk.content.response.ContentSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public List<ContentSummaryResponse> list(Authentication authentication) {
        return contentService.listCatalog(authentication.getName());
    }

    @GetMapping("/{id}")
    public ContentDetailResponse getById(@PathVariable String id, Authentication authentication) {
        return contentService.getById(id, authentication.getName());
    }
}
