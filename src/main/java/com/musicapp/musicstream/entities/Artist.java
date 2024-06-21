package com.musicapp.musicstream.entities;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false)
    private Date dateOfBirth;

    private String country;

    private int age;

    @OneToMany(mappedBy= "artisId", cascade = CascadeType.ALL)
    private List<?> singleSongList;

    @OneToMany(mappedBy= "artisId",cascade = CascadeType.ALL)
    private List<?> albumList;



}
