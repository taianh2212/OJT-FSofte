package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TourImages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "ImageID", nullable = false, unique = true))
public class TourImage extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID")
    private Tour tour;

    @Column(name = "ImageURL", length = 255)
    private String imageUrl;
}
