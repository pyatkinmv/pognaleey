package ru.pyatkinmv.pognaleey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_guides")
public class TravelGuide {
    @Id
    private Long id;

    private Long recommendationId;

    @Nullable
    private Long userId;

    private Instant createdAt;

    @Nullable
    private String title;

    @Nullable
    private Long imageId;

    private Language language;
}