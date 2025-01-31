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
@Table("travel_inquiries")
public class TravelInquiry {
  @Id private Long id;

  private String params;

  private Instant createdAt;

  private Long userId;
}
