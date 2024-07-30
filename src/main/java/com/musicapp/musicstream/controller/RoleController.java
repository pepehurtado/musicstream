package com.musicapp.musicstream.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.musicapp.musicstream.entities.Role;
import com.musicapp.musicstream.repository.PermissionRepository;
import com.musicapp.musicstream.repository.RoleRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role", description = "Operations related to Role")

public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    //Autorizar a los usuarios con el rol USER o ADMIN
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @GetMapping
    public List<Role> getAllRoles() {
        return (List<Role>) roleRepository.findAll();
    }

    
    @PreAuthorize("hasRole('USER')") 
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        //Recorre la lista de permisos y los busca en la base de datos por id, si lo encuentra, lo guarda en la lista de permisos del rol
        //Crea una lista de permisos vacia
        List<Permission> permissions = new ArrayList<>();
        role.getPermissions().forEach(permission -> {
            Optional<Permission> permissionOptional = permissionRepository.findById(permission.getId());
            permissionOptional.ifPresent(permissions::add);
        });
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Integer id, @RequestBody Role roleDetails) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isPresent()) {
            Role existingRole = role.get();
            existingRole.setName(roleDetails.getName());
            existingRole.setPermissions(roleDetails.getPermissions());
            return ResponseEntity.ok(roleRepository.save(existingRole));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isPresent()) {
            //Elimina la relacion de rol con usuario
            role.get().getUsers().forEach(user -> user.getRoles().remove(role.get()));
            roleRepository.delete(role.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
