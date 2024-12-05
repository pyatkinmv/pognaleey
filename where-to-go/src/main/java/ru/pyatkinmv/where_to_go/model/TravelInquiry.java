package ru.pyatkinmv.where_to_go.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("TRAVEL_INQUIRIES")
public class TravelInquiry {
    @Id
    private Long id;

    private String payload;

//    private TravelInquiryStatus status;

    private Instant createdAt;
}
