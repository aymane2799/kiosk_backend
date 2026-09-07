package com.example.kiosk.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    List<Favorite> findByUserId(String id);
    Optional<Favorite> findByUserIdAndContentId(String userId, String contentId);

    @Query("SELECT f.content.id FROM Favorite f WHERE f.user.id = :userId")
    Set<String> findContentIdsByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndContentId(String userId, String contentId);
}
