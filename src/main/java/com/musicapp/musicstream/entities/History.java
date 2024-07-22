package com.musicapp.musicstream.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "history")
@Data
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type; // Tipo de entidad, por ejemplo: "artist", "album", "song"

    @Column(nullable = false)
    private Integer idEntity; // ID de la entidad

    @Column(nullable = false)
    private Date timestamp; // Marca de tiempo del evento
}
