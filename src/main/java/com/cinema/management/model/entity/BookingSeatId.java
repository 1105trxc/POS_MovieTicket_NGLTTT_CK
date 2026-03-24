package com.cinema.management.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingSeatId implements Serializable {

    @Column(name = "ShowTimeID", length = 50)
    private String showTimeId;

    @Column(name = "SeatID", length = 50)
    private String seatId;
}
