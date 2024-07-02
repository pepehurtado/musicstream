package com.musicapp.musicstream.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;

@Service
public class DTOUtils {

    public SongDTO convertToDto(Song song) {
        SongDTO songDTO = new SongDTO();
        songDTO.setId(song.getId());
        
        songDTO.setTitle(song.getTitle());
        
        songDTO.setTime(song.getTime());
        songDTO.setUrl(song.getUrl());
        //convertimos los artistas a dto
        Set<Integer> artists = song.getArtists().stream().map(Artist::getId).collect(Collectors.toSet());
        System.out.println(artists);
        songDTO.setArtists(artists);
        if (song.getAlbum() != null) {
            AlbumDTO albumDTO = new AlbumDTO();
            albumDTO.setId(song.getAlbum().getId());
            songDTO.setAlbum(albumDTO.getId());
        }
        //Convertimos los géneros a dto
        Set<Integer> genreList = song.getGenreList().stream().map(Genre::getId).collect(Collectors.toSet());
        songDTO.setGenreList(genreList);
        return songDTO;
    }

    public ArtistDTO convertToDto(Artist artist) {
        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setId(artist.getId());
        artistDTO.setName(artist.getName());
        artistDTO.setDateOfBirth(artist.getDateOfBirth());
        artistDTO.setCountry(artist.getCountry());
        artistDTO.setAge(artist.getAge());
        //Convertimos las canciones a dto
        Set<SongDTO> singleSongList = artist.getSingleSongList().stream().map(this::convertToDto).collect(Collectors.toSet());
        artistDTO.setSingleSongList(singleSongList);
        //Convertimos los álbumes a dto
        Set<AlbumDTO> albumList = artist.getAlbums().stream().map(this::convertToDto).collect(Collectors.toSet());
        artistDTO.setAlbumList(albumList);
        return artistDTO;
    }

    public AlbumDTO convertToDto(Album album) {
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setId(album.getId());
        albumDTO.setTitle(album.getTitle());
        albumDTO.setYear(album.getYear());
        albumDTO.setArtist(album.getArtist().getId());
        albumDTO.setDescription(album.getDescription());
        albumDTO.setNumberOfSongs(album.getNumberOfSongs());
        albumDTO.setUrl(album.getUrl());
        //Guardarm los ids de las canciones
        Set<Integer> songs = album.getSongs().stream().map(Song::getId).collect(Collectors.toSet());
        albumDTO.setSongs(songs);
        return albumDTO;
    }
    public GenreDTO convertToDto(Genre genre) {
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setId(genre.getId());
        genreDTO.setName(genre.getName());
        Set<Integer> songs = genre.getSongList().stream().map(Song::getId).collect(Collectors.toSet());
        genreDTO.setSongList(songs);
        return genreDTO;
    }

}
