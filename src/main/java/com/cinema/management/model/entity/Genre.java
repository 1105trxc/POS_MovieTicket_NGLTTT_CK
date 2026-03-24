package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Genre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @Column(name = "GenreID", length = 50)
    private String genreId;

    @Column(name = "GenreName", nullable = false, length = 100)
    private String genreName;

    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<MovieGenre> movieGenres;
}
