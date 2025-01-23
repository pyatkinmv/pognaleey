package ru.pyatkinmv.pognaleey.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_recommendations")
@ToString(onlyExplicitlyIncluded = true)
public class TravelRecommendation {
    @ToString.Include
    @Id
    private Long id;

    private Instant createdAt;

    @ToString.Include
    private Long inquiryId;

    @ToString.Include
    private String title;

    @Nullable
    private Long imageId;

    /**
     * {@link GptResponseRecommendationDetailsDto} stored here
     */
    @Nullable
    private String details;

    // TODO: Подумать насчет статуса Inquiry вместо этого
    private ProcessingStatus status;
}
