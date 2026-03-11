package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Wishlist", uniqueConstraints = { @UniqueConstraint(columnNames = { "UserID", "TourID" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "WishlistID", nullable = false, unique = true))
public class Wishlist extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID")
    private Tour tour;
}
