package com.musicapp.musicstream.entities;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artist")
@Data
@NoArgsConstructor
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private Date dateOfBirth;

    private String country;

    private int age;

    @ManyToMany
    @JoinTable(
        name = "song_artist",
        joinColumns = { @JoinColumn(name = "artist_id") },
        inverseJoinColumns = { @JoinColumn(name = "song_id") }
    )
    private Set<Song> singleSongList;

    @OneToMany(mappedBy = "artist")
    private Set<Album> albums;
}