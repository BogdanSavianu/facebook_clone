package com.utcn.eventservice.repository;

import com.utcn.eventservice.entity.EventAttendee;
import com.utcn.eventservice.entity.EventAttendee.EventAttendeeId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAttendeeRepository extends CrudRepository<EventAttendee, EventAttendeeId> {
    List<EventAttendee> findByIdMemberId(Integer memberId);
    List<EventAttendee> findByIdEventId(Integer eventId);
} 