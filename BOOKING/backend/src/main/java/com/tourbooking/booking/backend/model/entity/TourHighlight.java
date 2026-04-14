package com.tourbooking.booking.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TourHighlights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "HighlightID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class TourHighlight extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID", columnDefinition = "BIGINT")
    private Tour tour;

    @Column(name = "Highlight", length = 255)
    private String highlight;
}
