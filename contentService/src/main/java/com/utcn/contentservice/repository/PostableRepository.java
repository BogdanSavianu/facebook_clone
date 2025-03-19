package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.Postable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostableRepository extends CrudRepository<Postable, Integer> {
    List<Postable> findByUserIdAndTitleIsNotNullAndParentIsNull(Integer userId);
    List<Postable> findByGroupIdAndTitleIsNotNullAndParentIsNull(Integer groupId);
} 