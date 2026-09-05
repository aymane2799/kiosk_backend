package com.example.kiosk.favorite;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.common.Auditable;
import com.example.kiosk.content.Content;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite extends Auditable {
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
}
