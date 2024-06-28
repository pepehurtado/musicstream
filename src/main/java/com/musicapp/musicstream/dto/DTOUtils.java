package com.musicapp.musicstream.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
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
        Set<ArtistDTO> artists = song.getArtists().stream().map(this::convertToDto).collect(Collectors.toSet());
        System.out.println(artists);
        songDTO.setArtists(artists);
        if (song.getAlbum() != null) {
            AlbumDTO albumDTO = new AlbumDTO();
            albumDTO.setId(song.getAlbum().getId());
            songDTO.setAlbum(albumDTO);
        }
        return songDTO;
    }

    public ArtistDTO convertToDto(Artist artist) {
        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setId(artist.getId());
        artistDTO.setName(artist.getName());
        artistDTO.setDateOfBirth(artist.getDateOfBirth());
        artistDTO.setCountry(artist.getCountry());
        artistDTO.setAge(artist.getAge());
        return artistDTO;
    }

    public AlbumDTO convertToDto(Album album) {
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setId(album.getId());
        albumDTO.setTitle(album.getTitle());
        albumDTO.setYear(album.getYear());
        albumDTO.setArtist(convertToDto(album.getArtist()));
        albumDTO.setDescription(album.getDescription());
        albumDTO.setNumberOfSongs(album.getNumberOfSongs());
        albumDTO.setUrl(album.getUrl());
        Set<SongDTO> songs = album.getSongs().stream().map(this::convertToDto).collect(Collectors.toSet());
        albumDTO.setSongs(songs);
        return albumDTO;
    }
}
