package com.example.kiosk.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    List<Favorite> findAllByUserId(String id);
    Optional<Favorite> findByUserIdAndContentId(String userId, String contentId);
}
