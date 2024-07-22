package com.musicapp.musicstream.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.musicapp.musicstream.entities.History;

import jakarta.transaction.Transactional;

public interface HistoryRepository extends CrudRepository<History, Integer>, JpaSpecificationExecutor<History> {
    @Query("SELECT h FROM History h WHERE h.type = :type AND h.timestamp <= :date")
    List<History> findByTypeAndDateBefore(@Param("type") String type, @Param("date") Date date);

    @Query("SELECT COUNT(h) FROM History h WHERE h.type = :type")
    Integer countByType(@Param("type") String type);

      @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.type = :type AND h.idEntity = :idEntity")
    void deleteByTypeAndIdEntity(@Param("type") String type, @Param("idEntity") Integer idEntity);
}

