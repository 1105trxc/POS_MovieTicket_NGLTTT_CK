package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "SeatType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatType {

    @Id
    @Column(name = "SeatTypeID", length = 50)
    private String seatTypeId;

    @Column(name = "TypeName", nullable = false, length = 100)
    private String typeName;

    @Column(name = "BasePrice", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "seatType", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Seat> seats;
}
