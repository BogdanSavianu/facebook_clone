package com.utcn.eventservice.controller;

import com.utcn.eventservice.entity.EventAttendee;
import com.utcn.eventservice.entity.EventAttendee.EventAttendeeId;
import com.utcn.eventservice.service.EventAttendeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendees")
public class EventAttendeeController {

    @Autowired
    private EventAttendeeService eventAttendeeService;

    @GetMapping
    public ResponseEntity<List<EventAttendee>> getAllAttendees() {
        return ResponseEntity.ok(eventAttendeeService.retrieveAttendees());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventAttendee>> getAttendeesByEventId(@PathVariable Integer eventId) {
        return ResponseEntity.ok(eventAttendeeService.retrieveAttendeesByEventId(eventId));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<EventAttendee>> getAttendeesByMemberId(@PathVariable Integer memberId) {
        return ResponseEntity.ok(eventAttendeeService.retrieveAttendeesByMemberId(memberId));
    }

    @PostMapping
    public ResponseEntity<EventAttendee> createAttendee(@RequestBody EventAttendee attendee) {
        return new ResponseEntity<>(eventAttendeeService.addAttendee(attendee), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<EventAttendee> updateAttendee(@RequestBody EventAttendee attendee) {
        EventAttendee existingAttendee = eventAttendeeService.retrieveAttendeeById(attendee.getId());
        if (existingAttendee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(eventAttendeeService.updateAttendee(attendee));
    }

    @DeleteMapping("/{eventId}/{memberId}")
    public ResponseEntity<Void> deleteAttendee(@PathVariable Integer eventId, @PathVariable Integer memberId) {
        EventAttendeeId id = new EventAttendeeId(memberId, eventId);
        EventAttendee existingAttendee = eventAttendeeService.retrieveAttendeeById(id);
        if (existingAttendee == null) {
            return ResponseEntity.notFound().build();
        }
        eventAttendeeService.deleteAttendee(id);
        return ResponseEntity.noContent().build();
    }
} 