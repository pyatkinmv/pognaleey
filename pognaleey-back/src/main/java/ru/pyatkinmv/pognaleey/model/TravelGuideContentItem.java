package ru.pyatkinmv.pognaleey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_guide_content_items")
public class TravelGuideContentItem {
    @Id
    private Long id;

    private Long guideId;

    @Nullable
    private String content;

    private Integer ordinal;

    private ProcessingStatus status;
}
