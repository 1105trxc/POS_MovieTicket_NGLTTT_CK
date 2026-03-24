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
public class MovieGenreId implements Serializable {

    @Column(name = "MovieID", length = 50)
    private String movieId;

    @Column(name = "GenreID", length = 50)
    private String genreId;
}
