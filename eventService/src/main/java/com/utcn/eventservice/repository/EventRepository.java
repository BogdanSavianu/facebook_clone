package com.utcn.eventservice.repository;

import com.utcn.eventservice.entity.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Integer> {
    List<Event> findByGroupId(Integer groupId);
} 