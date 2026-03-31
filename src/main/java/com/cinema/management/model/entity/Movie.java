package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Movie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @Column(name = "MovieID", length = 50)
    private String movieId;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Duration", nullable = false)
    private Integer duration; // in minutes

    @Column(name = "ReleaseDate")
    private LocalDate releaseDate;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ShowTime> showTimes;

    @OneToMany(mappedBy = "applyToMovie", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Promotion> promotions;

    @Column(name = "AgeRestriction", length = 10)
    private String ageRestriction;
}
