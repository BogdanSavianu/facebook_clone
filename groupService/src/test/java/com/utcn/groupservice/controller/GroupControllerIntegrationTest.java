package com.utcn.groupservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.groupservice.entity.Group;
import com.utcn.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Group testGroup;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
        
        testGroup = new Group();
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setPrivacySetting("PUBLIC");
        testGroup = groupRepository.save(testGroup);
    }

    @Test
    void getAllGroups_ShouldReturnListOfGroups() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Group")))
                .andExpect(jsonPath("$[0].description", is("Test Description")))
                .andExpect(jsonPath("$[0].privacySetting", is("PUBLIC")));
    }

    @Test
    void getGroupById_WithValidId_ShouldReturnGroup() throws Exception {
        mockMvc.perform(get("/groups/{id}", testGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Group")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.privacySetting", is("PUBLIC")));
    }

    @Test
    void getGroupById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/groups/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchGroupsByName_ShouldReturnMatchingGroups() throws Exception {
        mockMvc.perform(get("/groups/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Group")));
    }

    @Test
    void createGroup_ShouldReturnCreatedGroup() throws Exception {
        Group newGroup = new Group();
        newGroup.setName("New Group");
        newGroup.setDescription("New Description");
        newGroup.setPrivacySetting("PRIVATE");

        mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newGroup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Group")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.privacySetting", is("PRIVATE")));
    }

    @Test
    void updateGroup_WithValidId_ShouldUpdateGroup() throws Exception {
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group");
        updatedGroup.setDescription("Updated Description");
        updatedGroup.setPrivacySetting("FRIENDS");

        mockMvc.perform(put("/groups/{id}", testGroup.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedGroup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Group")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.privacySetting", is("FRIENDS")));
    }

    @Test
    void updateGroup_WithInvalidId_ShouldReturn404() throws Exception {
        Group updatedGroup = new Group();
        updatedGroup.setName("Updated Group");
        updatedGroup.setDescription("Updated Description");
        updatedGroup.setPrivacySetting("PUBLIC");

        mockMvc.perform(put("/groups/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedGroup)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup_WithValidId_ShouldDeleteGroup() throws Exception {
        mockMvc.perform(delete("/groups/{id}", testGroup.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/groups/{id}", testGroup.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/groups/{id}", 999))
                .andExpect(status().isNotFound());
    }
} 