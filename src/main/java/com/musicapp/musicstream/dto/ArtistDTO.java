package com.musicapp.musicstream.dto;

import java.sql.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArtistDTO {
    private Integer id;
    private String name;
    private Date dateOfBirth;
    private String country;
    private Integer age;

}
