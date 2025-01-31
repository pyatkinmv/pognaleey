package ru.pyatkinmv.pognaleey.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_guides_likes")
public class TravelGuideLike {
  @Id private Long id;

  private Instant createdAt;

  private Long userId;

  private Long guideId;
}
