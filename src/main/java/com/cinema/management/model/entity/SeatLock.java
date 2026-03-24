package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SeatLock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLock {

    @EmbeddedId
    private SeatLockId id;

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
    @JoinColumn(name = "LockedBy", nullable = false)
    @ToString.Exclude
    private User lockedBy;

    @Column(name = "LockedAt")
    @Builder.Default
    private LocalDateTime lockedAt = LocalDateTime.now();

    /**
     * Thời điểm hết hạn khóa ghế = LockedAt + 15 phút (BR-04).
     * Phải dùng giờ Server: LocalDateTime.now() — không dùng giờ máy trạm (BR-03).
     */
    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;
}
