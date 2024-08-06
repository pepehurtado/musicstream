package com.musicapp.musicstream.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.entities.Permission;
import com.musicapp.musicstream.entities.PermissionTranslater;
import com.musicapp.musicstream.repository.PermissionRepository;
import com.musicapp.musicstream.repository.PermissionTranslaterRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/translaters")
@Tag(name = "PermissionTranslater", description = "Operations related to translate Permission")
public class PermissionTranslaterController {

    @Autowired
    private PermissionTranslaterRepository permissionTranslaterRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    public List<PermissionTranslater> getAllPermissions() {
        return (List<PermissionTranslater>) permissionTranslaterRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionTranslater> getPermissionById(@PathVariable Integer id) {
        Optional<PermissionTranslater> permission = permissionTranslaterRepository.findById(id);
        return permission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public PermissionTranslater createPermission(@RequestBody PermissionTranslater permissionDetails) {
        //Buscar el permiso por id y si no existe lanzar excepcion
        Permission permission = permissionRepository.findById(permissionDetails.getPermission().getId())
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return permissionTranslaterRepository.save(permissionDetails);
    }

    //Hacer metodo para buscar por lenguaje
    @GetMapping("/language/{language}")
    public List<PermissionTranslater> getPermissionByLanguage(@PathVariable String language) {
        return permissionTranslaterRepository.findByLanguage(language);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionTranslater> updatePermission(@PathVariable Integer id, @RequestBody PermissionTranslater permissionDetails) {
        Optional<PermissionTranslater> permission = permissionTranslaterRepository.findById(id);
        if (permission.isPresent()) {
            PermissionTranslater existingPermission = permission.get();
            existingPermission.setPermission(permissionDetails.getPermission());
            existingPermission.setLanguage(permissionDetails.getLanguage());
            existingPermission.setTranslation(permissionDetails.getTranslation());
            return ResponseEntity.ok(permissionTranslaterRepository.save(existingPermission));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        Optional<PermissionTranslater> permission = permissionTranslaterRepository.findById(id);
        if (permission.isPresent()) {
            permissionTranslaterRepository.delete(permission.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //Hacer un get para buscar por permiso e idioma
    @GetMapping("/permission/{permissionId}/language/{language}")
    public ResponseEntity<PermissionTranslater> getPermissionByPermissionAndLanguage(@PathVariable Integer permissionId, @PathVariable String language) {
        PermissionTranslater permission = permissionTranslaterRepository.findByPermissionIdAndLanguage(permissionId, language);
        return permission != null ? ResponseEntity.ok(permission) : ResponseEntity.notFound().build();
    }
}
