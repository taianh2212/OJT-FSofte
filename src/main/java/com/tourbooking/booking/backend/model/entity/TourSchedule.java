package com.tourbooking.booking.backend.model.entity;

import com.tourbooking.booking.backend.model.entity.enums.TourStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "TourSchedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "ScheduleID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class TourSchedule extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID", columnDefinition = "BIGINT")
    private Tour tour;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "AvailableSlots")
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private TourStatus status = TourStatus.OPEN;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
