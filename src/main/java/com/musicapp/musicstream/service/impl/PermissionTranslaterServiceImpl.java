package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.entities.Permission;
import com.musicapp.musicstream.entities.PermissionTranslater;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.PermissionRepository;
import com.musicapp.musicstream.repository.PermissionTranslaterRepository;
import com.musicapp.musicstream.service.PermissionTranslaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionTranslaterServiceImpl implements PermissionTranslaterService {

    private final PermissionTranslaterRepository permissionTranslaterRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionTranslaterServiceImpl(PermissionTranslaterRepository permissionTranslaterRepository,
                                         PermissionRepository permissionRepository) {
        this.permissionTranslaterRepository = permissionTranslaterRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<PermissionTranslater> getAllPermissionTranslations() {
        return (List<PermissionTranslater>) permissionTranslaterRepository.findAll();
    }

    @Override
    public ResponseEntity<PermissionTranslater> getPermissionTranslationById(Integer id) {
        Optional<PermissionTranslater> translation = permissionTranslaterRepository.findById(id);
        return translation.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public PermissionTranslater createPermissionTranslation(PermissionTranslater permissionTranslater) {
        Permission permission = permissionRepository.findById(permissionTranslater.getPermission().getId())
                .orElseThrow(() -> new ApiRuntimeException("Permission not found with id: " + permissionTranslater.getPermission().getId(),404));
        
        permissionTranslater.setPermission(permission);
        return permissionTranslaterRepository.save(permissionTranslater);
    }

    @Override
    public List<PermissionTranslater> getPermissionTranslationsByLanguage(String language) {
        return permissionTranslaterRepository.findByLanguage(language);
    }

    @Override
    public ResponseEntity<PermissionTranslater> updatePermissionTranslation(
            Integer id, PermissionTranslater permissionTranslater) {
        Optional<PermissionTranslater> existingTranslation = permissionTranslaterRepository.findById(id);
        
        if (existingTranslation.isPresent()) {
            PermissionTranslater translationToUpdate = existingTranslation.get();
            translationToUpdate.setLanguage(permissionTranslater.getLanguage());
            translationToUpdate.setTranslation(permissionTranslater.getTranslation());
            
            // Actualizar la relación con Permission si es necesario
            if (permissionTranslater.getPermission() != null) {
                Permission permission = permissionRepository.findById(permissionTranslater.getPermission().getId())
                        .orElseThrow(() -> new ApiRuntimeException("Permission not found with id: " + permissionTranslater.getPermission().getId(), 404));
                translationToUpdate.setPermission(permission);
            }
            
            return ResponseEntity.ok(permissionTranslaterRepository.save(translationToUpdate));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Void> deletePermissionTranslation(Integer id) {
        Optional<PermissionTranslater> translation = permissionTranslaterRepository.findById(id);
        if (translation.isPresent()) {
            permissionTranslaterRepository.delete(translation.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<PermissionTranslater> getPermissionTranslationByPermissionAndLanguage(
            Integer permissionId, String language) {
        PermissionTranslater translation = permissionTranslaterRepository.findByPermissionIdAndLanguage(permissionId, language);
        return translation != null ? ResponseEntity.ok(translation) : ResponseEntity.notFound().build();
    }
}