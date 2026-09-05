package com.example.kiosk.content;

import com.example.kiosk.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "content")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content extends Auditable {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "excerpt", length = 1000)
    private String excerpt;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "category", nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private ContentTier tier;

    @Column(name = "published_at")
    private Instant publishedAt;
}
