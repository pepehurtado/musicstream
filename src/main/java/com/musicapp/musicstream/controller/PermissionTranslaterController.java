package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.entities.PermissionTranslater;
import com.musicapp.musicstream.service.PermissionTranslaterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/translaters")
@Tag(name = "PermissionTranslater", description = "Operations related to translate Permission")
public class PermissionTranslaterController {

    @Autowired
    private PermissionTranslaterService permissionTranslaterService;

    @GetMapping
    public List<PermissionTranslater> getAllPermissionTranslations() {
        return permissionTranslaterService.getAllPermissionTranslations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionTranslater> getPermissionTranslationById(@PathVariable Integer id) {
        return permissionTranslaterService.getPermissionTranslationById(id);
    }

    @PostMapping
    public PermissionTranslater createPermissionTranslation(@RequestBody PermissionTranslater permissionTranslater) {
        return permissionTranslaterService.createPermissionTranslation(permissionTranslater);
    }

    @GetMapping("/language/{language}")
    public List<PermissionTranslater> getPermissionTranslationsByLanguage(@PathVariable String language) {
        return permissionTranslaterService.getPermissionTranslationsByLanguage(language);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionTranslater> updatePermissionTranslation(
            @PathVariable Integer id, 
            @RequestBody PermissionTranslater permissionTranslater) {
        return permissionTranslaterService.updatePermissionTranslation(id, permissionTranslater);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermissionTranslation(@PathVariable Integer id) {
        return permissionTranslaterService.deletePermissionTranslation(id);
    }

    @GetMapping("/permission/{permissionId}/language/{language}")
    public ResponseEntity<PermissionTranslater> getPermissionTranslationByPermissionAndLanguage(
            @PathVariable Integer permissionId, 
            @PathVariable String language) {
        return permissionTranslaterService.getPermissionTranslationByPermissionAndLanguage(permissionId, language);
    }
}