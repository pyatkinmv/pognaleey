package ru.pyatkinmv.pognaleey.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelInquiryDto {
  private Long id;
  private String params;
  private Instant createdAt;
}
