package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MovieGenre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieGenre {

    @EmbeddedId
    private MovieGenreId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "MovieID")
    @ToString.Exclude
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "GenreID")
    @ToString.Exclude
    private Genre genre;
}
