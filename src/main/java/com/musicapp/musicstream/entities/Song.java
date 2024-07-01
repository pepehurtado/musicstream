package com.musicapp.musicstream.entities;

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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "song")
@Data
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private Integer time;

    private String url;

    
    @ManyToMany(mappedBy = "singleSongList")
    private Set<Artist> artists = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;


    @ManyToMany(mappedBy = "songList",cascade=CascadeType.ALL)
    private Set<Genre> genreList = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", time=" + time +
                ", url='" + url + '\'' +
                ", album=" + (album != null ? album.getId() : null) + // Evitar impresión completa del álbum
                ", genreList=" + genreList +
                '}';
    }
}
