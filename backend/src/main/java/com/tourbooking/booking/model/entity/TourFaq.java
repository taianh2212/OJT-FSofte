package com.tourbooking.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TourFaqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "FaqID", nullable = false, unique = true, columnDefinition = "BIGINT"))
public class TourFaq extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TourID", columnDefinition = "BIGINT")
    private Tour tour;

    @Column(name = "Question", length = 500)
    private String question;

    @Column(name = "Answer", columnDefinition = "NVARCHAR(MAX)")
    private String answer;
}
