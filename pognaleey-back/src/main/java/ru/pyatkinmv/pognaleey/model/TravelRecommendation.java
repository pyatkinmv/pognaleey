package ru.pyatkinmv.pognaleey.model;

import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.pyatkinmv.pognaleey.dto.GptResponseRecommendationDetailsDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_recommendations")
@ToString(onlyExplicitlyIncluded = true)
public class TravelRecommendation {
  @ToString.Include @Id private Long id;

  private Instant createdAt;

  @ToString.Include private Long inquiryId;

  @ToString.Include private String title;

  @Nullable private Long imageId;

  /** {@link GptResponseRecommendationDetailsDto} stored here */
  @Nullable private String details;

  private ProcessingStatus status;
}
