package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.entities.History;
import com.musicapp.musicstream.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/entities")
    public List<History> getEntitiesByTypeAndDate(
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        return historyService.getEntitiesByTypeAndDate(type, date);
    }

    @GetMapping("/count")
    public Map<String, Integer> getEntityCounts() {
        return historyService.getEntityCounts();
    }
}