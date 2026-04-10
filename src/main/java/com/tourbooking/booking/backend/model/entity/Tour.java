package com.tourbooking.booking.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "Tours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "TourID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class Tour extends Base {

    @Column(name = "TourName", columnDefinition = "NVARCHAR(200)")
    private String tourName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "Price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "OriginalPrice", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "Inclusions", columnDefinition = "NVARCHAR(MAX)")
    private String inclusions;

    @Column(name = "Exclusions", columnDefinition = "NVARCHAR(MAX)")
    private String exclusions;

    @Column(name = "Tips", columnDefinition = "NVARCHAR(MAX)")
    private String tips;

    @Column(name = "Itinerary", columnDefinition = "NVARCHAR(MAX)")
    private String itinerary;

    @Column(name = "PaymentPolicy", columnDefinition = "NVARCHAR(MAX)")
    private String paymentPolicy;

    @Column(name = "CancellationPolicy", columnDefinition = "NVARCHAR(MAX)")
    private String cancellationPolicy;

    @Column(name = "ChildPolicy", columnDefinition = "NVARCHAR(MAX)")
    private String childPolicy;

    @Builder.Default
    @Column(name = "HasPickup")
    private Boolean hasPickup = false;

    @Builder.Default
    @Column(name = "HasLunch")
    private Boolean hasLunch = false;

    @Builder.Default
    @Column(name = "IsInstantConfirmation")
    private Boolean isInstantConfirmation = false;

    @Builder.Default
    @Column(name = "IsDaily")
    private Boolean isDaily = false;

    @Column(name = "MinDepositRate", precision = 5, scale = 2)
    private BigDecimal minDepositRate;

    @Column(name = "RefundGracePeriod")
    private Integer refundGracePeriod;

    @Column(name = "MetaTitle", columnDefinition = "NVARCHAR(200)")
    private String metaTitle;

    @Column(name = "MetaDescription", columnDefinition = "NVARCHAR(500)")
    private String metaDescription;

    @Column(name = "WhyChooseUs", columnDefinition = "NVARCHAR(MAX)")
    private String whyChooseUs;

    @Column(name = "SuitableAges", columnDefinition = "NVARCHAR(MAX)")
    private String suitableAges;

    @Column(name = "BestTime", columnDefinition = "NVARCHAR(MAX)")
    private String bestTime;

    @Column(name = "WeatherInfo", columnDefinition = "NVARCHAR(MAX)")
    private String weatherInfo;

    @Column(name = "GuideInfo", columnDefinition = "NVARCHAR(MAX)")
    private String guideInfo;

    @Column(name = "Duration")
    private Integer duration;

    @Column(name = "StartLocation", columnDefinition = "NVARCHAR(100)")
    private String startLocation;

    @Column(name = "EndLocation", columnDefinition = "NVARCHAR(100)")
    private String endLocation;

    @Column(name = "Latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "Longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "TransportType", columnDefinition = "NVARCHAR(50)")
    private String transportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", columnDefinition = "BIGINT")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CityID", columnDefinition = "BIGINT", nullable = true)
    private City city;

    @Builder.Default
    @Column(name = "Rating")
    private Double rating = 0.0;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TourImage> images;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TourSchedule> schedules;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TourHighlight> highlights;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TourFaq> faqs;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Review> reviews;

    @Builder.Default
    @Column(name = "Source", columnDefinition = "NVARCHAR(50)")
    private String source = "LOCAL";

    @Column(name = "ExternalId", length = 100)
    private String externalId;
}
