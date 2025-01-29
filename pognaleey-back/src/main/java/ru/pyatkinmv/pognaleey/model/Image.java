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
@Table("images")
public class Image {
    @Id
    private Long id;

    private Instant createdAt;
    private String title;
    @Nullable
    private String url;
    @Nullable
    private String thumbnailUrl;
    private String query;
    private Boolean aiGenerated;
    @Nullable
    private String licenceUrl;
    @Nullable
    private String authorName;
    @Nullable
    private String authorUrl;
}
