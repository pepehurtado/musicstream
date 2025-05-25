package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.entities.History;
import com.musicapp.musicstream.repository.HistoryRepository;
import com.musicapp.musicstream.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    @Autowired
    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<History> getEntitiesByTypeAndDate(String type, Date date) {
        return historyRepository.findByTypeAndDateBefore(type, date);
    }

    @Override
    public Map<String, Integer> getEntityCounts() {
        Map<String, Integer> counts = new HashMap<>();
        
        counts.put("artists", historyRepository.countByType("artist"));
        counts.put("songs", historyRepository.countByType("song"));
        counts.put("albums", historyRepository.countByType("album"));
        counts.put("genres", historyRepository.countByType("genres"));
        
        return counts;
    }
}