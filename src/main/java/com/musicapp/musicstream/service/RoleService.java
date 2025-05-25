package com.musicapp.musicstream.service;

import com.musicapp.musicstream.entities.Role;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    ResponseEntity<Role> getRoleById(Integer id);
    Role createRole(Role role);
    ResponseEntity<Role> updateRole(Integer id, Role role);
    ResponseEntity<Void> deleteRole(Integer id);
}