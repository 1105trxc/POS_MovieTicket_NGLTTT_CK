package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @Column(name = "RoomID", length = 50)
    private String roomId;

    @Column(name = "RoomName", nullable = false, length = 100)
    private String roomName;

    @Column(name = "Capacity", nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ShowTime> showTimes;
}
