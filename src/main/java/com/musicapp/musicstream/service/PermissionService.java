package com.musicapp.musicstream.service;

import com.musicapp.musicstream.entities.Permission;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();
    ResponseEntity<Permission> getPermissionById(Integer id);
    Permission createPermission(Permission permission);
    ResponseEntity<Permission> updatePermission(Integer id, Permission permission);
    ResponseEntity<Void> deletePermission(Integer id);
}