package com.musicapp.musicstream.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "album")
@Data
@NoArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String year;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    private String description;

    private Integer numberOfSongs;

    private String url;

    
    @OneToMany(mappedBy = "album")
    private Set<Song> songs;


    public void addSong(Song song) {
        if (songs == null) {
            songs = new HashSet<>();
        }
        songs.add(song);
        song.setAlbum(this);
    }

    public void removeSong(Song song) {
        songs.remove(song);
        song.setAlbum(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(id, album.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", description='" + description + '\'' +
                ", numberOfSongs=" + numberOfSongs +
                ", url='" + url + '\'' +
                ", artist=" + (artist != null ? artist.getId() : null) + // Evitar impresión completa del artista
                '}';
    }
}