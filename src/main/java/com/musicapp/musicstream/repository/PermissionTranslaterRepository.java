package com.musicapp.musicstream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.PermissionTranslater;



public interface PermissionTranslaterRepository extends CrudRepository<PermissionTranslater, Integer>, JpaSpecificationExecutor<PermissionTranslater> {
    List<PermissionTranslater> findByLanguage(String language);
    //Metodo para buscar por permiso e idioma
    PermissionTranslater findByPermissionIdAndLanguage(Integer permissionId, String language);
}
