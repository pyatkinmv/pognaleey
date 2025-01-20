package ru.pyatkinmv.pognaleey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelInquiryDto {
    private Long id;
    private String payload;
    private Instant createdAt;
}

