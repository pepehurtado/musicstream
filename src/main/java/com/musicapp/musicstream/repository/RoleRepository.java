package com.musicapp.musicstream.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Integer>, JpaSpecificationExecutor<Role> {
    Role findByName(String name);
}
