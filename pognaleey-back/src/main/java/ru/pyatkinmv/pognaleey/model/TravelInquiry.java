package ru.pyatkinmv.pognaleey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_inquiries")
public class TravelInquiry {
    @Id
    private Long id;

    private String params;

    private Instant createdAt;
}
