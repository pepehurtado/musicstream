package com.musicapp.musicstream.service;

import com.musicapp.musicstream.entities.PermissionTranslater;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PermissionTranslaterService {
    List<PermissionTranslater> getAllPermissionTranslations();
    ResponseEntity<PermissionTranslater> getPermissionTranslationById(Integer id);
    PermissionTranslater createPermissionTranslation(PermissionTranslater permissionTranslater);
    List<PermissionTranslater> getPermissionTranslationsByLanguage(String language);
    ResponseEntity<PermissionTranslater> updatePermissionTranslation(Integer id, PermissionTranslater permissionTranslater);
    ResponseEntity<Void> deletePermissionTranslation(Integer id);
    ResponseEntity<PermissionTranslater> getPermissionTranslationByPermissionAndLanguage(Integer permissionId, String language);
}