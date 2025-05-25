package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.entities.Permission;
import com.musicapp.musicstream.repository.PermissionRepository;
import com.musicapp.musicstream.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> getAllPermissions() {
        return (List<Permission>) permissionRepository.findAll();
    }

    @Override
    public ResponseEntity<Permission> getPermissionById(Integer id) {
        Optional<Permission> permission = permissionRepository.findById(id);
        return permission.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public ResponseEntity<Permission> updatePermission(Integer id, Permission permissionDetails) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isPresent()) {
            Permission existingPermission = permission.get();
            existingPermission.setName(permissionDetails.getName());
            existingPermission.setEntity(permissionDetails.getEntity());
            return ResponseEntity.ok(permissionRepository.save(existingPermission));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Void> deletePermission(Integer id) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isPresent()) {
            permissionRepository.delete(permission.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}