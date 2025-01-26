package ru.pyatkinmv.pognaleey.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.lang.Nullable;
import ru.pyatkinmv.pognaleey.model.TravelGuide;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TravelGuideRepository extends CrudRepository<TravelGuide, Long> {

    @Query(value = """
            SELECT g.id AS guideId, COUNT(l.id) AS likesCount
                        FROM travel_guides g
                        LEFT JOIN travel_guides_likes l ON g.id = l.guide_id
                        WHERE (:userId IS NOT NULL AND :userId = g.user_id) OR :userId IS NULL
                        GROUP BY g.id
                        ORDER BY likesCount DESC, g.id DESC
                        LIMIT :limit OFFSET :offset
            """, resultSetExtractorClass = GuideLikesExtractor.class)
    Map<Long, Integer> findTopGuides(@Param("userId") @Nullable Long userId, @Param("limit") int limit,
                                     @Param("offset") int offset);

    List<TravelGuide> findAllByIdIn(Collection<Long> ids);

    @Query(value = """
            SELECT g.id AS guideId, COUNT(gl.id) AS likesCount
            FROM travel_guides g
            LEFT JOIN travel_guides_likes gl ON g.id = gl.guide_id
            WHERE g.id IN (:guideIds)
            GROUP BY g.id
            """, resultSetExtractorClass = GuideLikesExtractor.class)
    Map<Long, Integer> countLikesByGuideId(@Param("guideIds") Collection<Long> guideIds);

    @Modifying
    @Query("""
            UPDATE travel_guides
            SET title = :title
            WHERE id = :guideId
            """)
    void updateTitle(long guideId, String title);

    int countAllByUserId(Long userId);

    @Query(value = """
            SELECT g.recommendation_id AS recommendationId, g.id AS guideId
            FROM travel_guides g
            WHERE g.recommendation_id IN (:recommendationsIds)
            """, resultSetExtractorClass = RecommendationToGuideExtractor.class)
    Map<Long, Long> getRecommendationToGuideMap(List<Long> recommendationsIds);

    class RecommendationToGuideExtractor implements ResultSetExtractor<Map<Long, Long>> {
        @Override
        public Map<Long, Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Long> result = new HashMap<>();
            while (rs.next()) {
                Long guideId = rs.getLong("guideId");
                Long recommendationId = rs.getLong("recommendationId");
                result.put(recommendationId, guideId);
            }
            return result;
        }
    }

    class GuideLikesExtractor implements ResultSetExtractor<Map<Long, Integer>> {

        @Override
        public Map<Long, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Integer> result = new HashMap<>();
            while (rs.next()) {
                Long guideId = rs.getLong("guideId");
                Integer likesCount = rs.getInt("likesCount");
                result.put(guideId, likesCount);
            }
            return result;
        }

    }
}
