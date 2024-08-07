package com.musicapp.musicstream.entities;

import java.sql.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "song_artist",
        joinColumns = { @JoinColumn(name = "artist_id") },
        inverseJoinColumns = { @JoinColumn(name = "song_id") }
    )
    private Set<Song> singleSongList;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    private Set<Album> albums;

    public boolean addSong(Song song) {
        if (singleSongList == null) {
            singleSongList = new HashSet<>();
        }
        return singleSongList.add(song);
    }

    public boolean removeSong(Song song) {
        
        return singleSongList.remove(song);
    }

    public boolean addAlbum(Album album) {
        if (albums == null) {
            albums = new HashSet<>();
        }
        return albums.add(album);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(id, artist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", age=" + age +
                ", country='" + country + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", name='" + name + '\'' +
                '}';
    }
}
