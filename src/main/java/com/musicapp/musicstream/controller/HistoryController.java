package com.musicapp.musicstream.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.entities.History;
import com.musicapp.musicstream.repository.HistoryRepository;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryRepository historyRepository;

    @GetMapping("/entities")
    public List<History> getEntitiesByTypeAndDate(
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        return historyRepository.findByTypeAndDateBefore(type, date);
    }

    @GetMapping("/count")
    public Map<String, Integer> getEntityCounts() {
        Map<String, Integer> counts = new HashMap<>();

        // Contar el número de entradas para cada tipo de entidad en la tabla history
        int artistCount = historyRepository.countByType("artist");
        counts.put("artists", artistCount);

        int songCount = historyRepository.countByType("song");
        counts.put("songs", songCount);

        int albumCount = historyRepository.countByType("album");
        counts.put("albums", albumCount);

        int genresCount = historyRepository.countByType("genres");
        counts.put("genres", genresCount);

        return counts;
    }
    
}
