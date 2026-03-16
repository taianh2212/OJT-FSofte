package com.tourbooking.booking.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "TourID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class Tour extends Base {

    @Column(name = "TourName", length = 200)
    private String tourName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "Price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "Duration")
    private Integer duration;

    @Column(name = "StartLocation", length = 100)
    private String startLocation;

    @Column(name = "EndLocation", length = 100)
    private String endLocation;

    @Column(name = "Latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "Longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", columnDefinition = "BIGINT")
    private Category category;

    @Column(name = "Rating")
    private Double rating = 0.0;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TourImage> images;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TourSchedule> schedules;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TourHighlight> highlights;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;
}
