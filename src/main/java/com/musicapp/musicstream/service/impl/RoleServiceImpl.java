package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.entities.Permission;
import com.musicapp.musicstream.entities.Role;
import com.musicapp.musicstream.repository.PermissionRepository;
import com.musicapp.musicstream.repository.RoleRepository;
import com.musicapp.musicstream.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, 
                         PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return (List<Role>) roleRepository.findAll();
    }

    @Override
    public ResponseEntity<Role> getRoleById(Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(ResponseEntity::ok)
                 .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public Role createRole(Role role) {
        List<Permission> validPermissions = validateAndGetPermissions(role.getPermissions());
        role.setPermissions(validPermissions);
        return roleRepository.save(role);
    }

    @Override
    public ResponseEntity<Role> updateRole(Integer id, Role roleDetails) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    existingRole.setName(roleDetails.getName());
                    List<Permission> validPermissions = validateAndGetPermissions(roleDetails.getPermissions());
                    existingRole.setPermissions(validPermissions);
                    Role updatedRole = roleRepository.save(existingRole);
                    return ResponseEntity.ok(updatedRole);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteRole(Integer id) {
        return roleRepository.findById(id)
                .map(role -> {
                    // Eliminar relaciones con usuarios
                    role.getUsers().forEach(user -> user.getRoles().remove(role));
                    roleRepository.delete(role);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private List<Permission> validateAndGetPermissions(List<Permission> permissions) {
        List<Permission> validPermissions = new ArrayList<>();
        if (permissions != null) {
            permissions.forEach(permission -> 
                permissionRepository.findById(permission.getId())
                    .ifPresent(validPermissions::add)
            );
        }
        return validPermissions;
    }
}