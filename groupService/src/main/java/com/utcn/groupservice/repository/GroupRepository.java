package com.utcn.groupservice.repository;

import com.utcn.groupservice.entity.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, Integer> {
    List<Group> findByNameContainingIgnoreCase(String name);
} 