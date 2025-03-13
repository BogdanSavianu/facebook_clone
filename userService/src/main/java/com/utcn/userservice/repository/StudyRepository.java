package com.utcn.userservice.repository;

import com.utcn.userservice.entity.Study;
import com.utcn.userservice.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends CrudRepository<Study, Integer> {
    List<Study> findByUser(User user);
    List<Study> findByUserId(Integer userId);
} 