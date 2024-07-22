package com.musicapp.musicstream.common;

import com.musicapp.musicstream.entities.History;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.musicapp.musicstream.repository.HistoryRepository;

@Service
public class HistoryVoid {

    @Autowired
    private HistoryRepository historyRepository;

    public History createEntry(String type, Integer idEntity) {
        History history = new History();
        history.setType(type);
        history.setIdEntity(idEntity);
        history.setTimestamp(new java.sql.Date(new Date().getTime()));
        return historyRepository.save(history);
    }

    public void deleteEntries(String type, Integer idEntity) {
        historyRepository.deleteByTypeAndIdEntity(type, idEntity);
    }
}
