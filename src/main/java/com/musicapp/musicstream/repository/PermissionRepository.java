package com.musicapp.musicstream.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Permission;

public interface PermissionRepository extends CrudRepository<Permission, Integer>, JpaSpecificationExecutor<Permission> {
}
