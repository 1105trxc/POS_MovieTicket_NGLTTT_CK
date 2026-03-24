package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ShowTime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowTime {

    @Id
    @Column(name = "ShowTimeID", length = 50)
    private String showTimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    @ToString.Exclude
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID", nullable = false)
    @ToString.Exclude
    private Room room;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<BookingSeat> bookingSeats;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SeatLock> seatLocks;
}
