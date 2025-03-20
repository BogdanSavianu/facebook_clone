package com.utcn.eventservice.service;

import com.utcn.eventservice.entity.Event;
import com.utcn.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setName("Test Event");
        testEvent.setGroupId(1);
        testEvent.setDate(LocalDateTime.now().plusDays(1));
        testEvent.setAllDay(false);
    }

    @Test
    void whenRetrieveEvents_thenReturnEventList() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));

        List<Event> events = eventService.retrieveEvents();

        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals(testEvent.getName(), events.get(0).getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void whenRetrieveEventById_thenReturnEvent() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));

        Event event = eventService.retrieveEventById(1);

        assertNotNull(event);
        assertEquals(testEvent.getId(), event.getId());
        assertEquals(testEvent.getName(), event.getName());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    void whenRetrieveEventsByGroupId_thenReturnEventList() {
        when(eventRepository.findByGroupId(1)).thenReturn(Arrays.asList(testEvent));

        List<Event> events = eventService.retrieveEventsByGroupId(1);

        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals(testEvent.getGroupId(), events.get(0).getGroupId());
        verify(eventRepository, times(1)).findByGroupId(1);
    }

    @Test
    void whenAddEvent_thenReturnSavedEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event savedEvent = eventService.addEvent(testEvent);

        assertNotNull(savedEvent);
        assertEquals(testEvent.getName(), savedEvent.getName());
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void whenUpdateEvent_thenReturnUpdatedEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event updatedEvent = eventService.updateEvent(testEvent);

        assertNotNull(updatedEvent);
        assertEquals(testEvent.getName(), updatedEvent.getName());
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void whenDeleteEventById_thenVerifyDeletion() {
        doNothing().when(eventRepository).deleteById(1);

        eventService.deleteEventById(1);

        verify(eventRepository, times(1)).deleteById(1);
    }
} 