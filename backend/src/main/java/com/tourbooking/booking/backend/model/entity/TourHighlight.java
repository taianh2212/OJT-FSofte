<<<<<<<< Updated upstream:backend/src/main/java/com/tourbooking/booking/model/entity/TourHighlight.java
﻿package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TourHighlights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "HighlightID", nullable = false, unique = true))
public class TourHighlight extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID")
    private Tour tour;

    @Column(name = "Highlight", length = 255)
    private String highlight;
}
========
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
>>>>>>>> Stashed changes:backend/src/main/java/com/tourbooking/booking/backend/model/entity/TourHighlight.java
