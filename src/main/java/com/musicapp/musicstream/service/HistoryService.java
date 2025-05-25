package com.musicapp.musicstream.service;

import com.musicapp.musicstream.entities.History;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface HistoryService {
    List<History> getEntitiesByTypeAndDate(String type, Date date);
    Map<String, Integer> getEntityCounts();
}