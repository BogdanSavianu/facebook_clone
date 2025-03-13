package com.utcn.eventservice.service;

import com.utcn.eventservice.entity.EventAttendee;
import com.utcn.eventservice.entity.EventAttendee.EventAttendeeId;
import com.utcn.eventservice.repository.EventAttendeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventAttendeeService {

    @Autowired
    private EventAttendeeRepository eventAttendeeRepository;

    public List<EventAttendee> retrieveAttendees() {
        List<EventAttendee> attendees = new ArrayList<>();
        eventAttendeeRepository.findAll().forEach(attendees::add);
        return attendees;
    }

    public EventAttendee retrieveAttendeeById(EventAttendeeId id) {
        Optional<EventAttendee> attendee = eventAttendeeRepository.findById(id);
        return attendee.orElse(null);
    }

    public List<EventAttendee> retrieveAttendeesByEventId(Integer eventId) {
        return eventAttendeeRepository.findByIdEventId(eventId);
    }

    public List<EventAttendee> retrieveAttendeesByMemberId(Integer memberId) {
        return eventAttendeeRepository.findByIdMemberId(memberId);
    }

    public EventAttendee addAttendee(EventAttendee attendee) {
        return eventAttendeeRepository.save(attendee);
    }

    public EventAttendee updateAttendee(EventAttendee attendee) {
        return eventAttendeeRepository.save(attendee);
    }

    public void deleteAttendee(EventAttendeeId id) {
        eventAttendeeRepository.deleteById(id);
    }
} 