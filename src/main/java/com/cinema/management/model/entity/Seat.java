package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @Column(name = "SeatID", length = 50)
    private String seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID", nullable = false)
    @ToString.Exclude
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SeatTypeID", nullable = false)
    @ToString.Exclude
    private SeatType seatType;

    @Column(name = "RowChar", nullable = false, length = 10)
    private String rowChar;

    @Column(name = "SeatNumber", nullable = false)
    private Integer seatNumber;

    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<BookingSeat> bookingSeats;

    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SeatLock> seatLocks;
}
