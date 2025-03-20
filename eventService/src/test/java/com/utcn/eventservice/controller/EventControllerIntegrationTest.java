package com.utcn.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.eventservice.entity.Event;
import com.utcn.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setGroupId(1);
        testEvent.setDate(LocalDateTime.now().plusDays(1));
        testEvent.setAllDay(false);
    }

    @Test
    void whenCreateEvent_thenReturn201() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(testEvent.getName())))
                .andExpect(jsonPath("$.groupId", is(testEvent.getGroupId())));
    }

    @Test
    void whenGetAllEvents_thenReturn200() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(savedEvent.getName())));
    }

    @Test
    void whenGetEventById_thenReturn200() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(get("/events/{id}", savedEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(savedEvent.getName())))
                .andExpect(jsonPath("$.groupId", is(savedEvent.getGroupId())));
    }

    @Test
    void whenGetEventsByGroupId_thenReturn200() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(get("/events/group/{groupId}", savedEvent.getGroupId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(savedEvent.getName())));
    }

    @Test
    void whenUpdateEvent_thenReturn200() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);
        savedEvent.setName("Updated Event Name");

        mockMvc.perform(put("/events/{id}", savedEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Event Name")));
    }

    @Test
    void whenDeleteEvent_thenReturn204() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(delete("/events/{id}", savedEvent.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/events/{id}", savedEvent.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetNonExistentEvent_thenReturn404() throws Exception {
        mockMvc.perform(get("/events/{id}", 999))
                .andExpect(status().isNotFound());
    }
} 