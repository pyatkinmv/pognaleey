package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;

import java.util.Optional;
import java.util.Set;


public interface TravelGuideLikeRepository extends CrudRepository<TravelGuideLike, Long> {
    int countByGuideId(long guideId);

    Optional<TravelGuideLike> findByUserIdAndGuideId(long userId, long guideId);

    @Query("""
            SELECT l.guide_id AS guideId
            FROM  travel_guides_likes l
            WHERE l.user_id = :userId
            ORDER BY l.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Set<Long> findGuidesIdsByUserId(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    int countByUserId(Long userId);
}
