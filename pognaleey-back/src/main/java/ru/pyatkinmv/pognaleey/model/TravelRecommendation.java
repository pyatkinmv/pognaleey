package ru.pyatkinmv.pognaleey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsDto;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_recommendations")
public class TravelRecommendation {
    @Id
    private Long id;

    private Instant createdAt;

    private Long inquiryId;

    private String title;

    private String shortDescription;

    /**
     * {@link GptResponseRecommendationDetailsDto} stored here
     */
    private String details;

    private String imageUrl;
}
