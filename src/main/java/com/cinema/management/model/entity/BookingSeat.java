package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "BookingSeat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @EmbeddedId
    private BookingSeatId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("showTimeId")
    @JoinColumn(name = "ShowTimeID")
    @ToString.Exclude
    private ShowTime showTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seatId")
    @JoinColumn(name = "SeatID")
    @ToString.Exclude
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InvoiceID", nullable = false)
    @ToString.Exclude
    private Invoice invoice;

    /**
     * Giá snapshot tại thời điểm thanh toán (FR-NF-01).
     * Không thay đổi khi giá gốc bị chỉnh sửa về sau.
     */
    @Column(name = "Price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
}
