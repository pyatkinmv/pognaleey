package ru.pyatkinmv.pognaleey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

/**
 * Represents a content item within a travel guide. This object is an essential part of the travel
 * guide that gets loaded and displayed on the frontend. Each content item has a specific type
 * (e.g., MARKDOWN or IMAGE) and can include associated content like text or image references.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("travel_guide_content_items")
public class TravelGuideContentItem {
  @Id private Long id;

  private Long guideId;

  /**
   * The content of this item. If the {@link #type} is MARKDOWN, it contains text in MARKDOWN
   * format. If the {@link #type} is IMAGE, it contains a string formatted as "{imageId=:id}", where
   * :id is the identifier of the image in the image table. Can be null if no content is provided.
   */
  @Nullable private String content;

  /**
   * The content of this item. If the {@link #type} is MARKDOWN, it contains text in MARKDOWN
   * format. If the {@link #type} is IMAGE, it contains a string formatted as "{imageId=:id}", where
   * :id is the identifier of the image in the image table. Can be null if no content is provided.
   */
  private Integer ordinal;

  private ProcessingStatus status;

  private GuideContentItemType type;
}
