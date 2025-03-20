package com.utcn.groupservice.service;

import com.utcn.groupservice.entity.Group;
import com.utcn.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new Group();
        testGroup.setId(1);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setPrivacySetting("PUBLIC");
    }

    @Test
    void getAllGroups_ShouldReturnListOfGroups() {
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findAll()).thenReturn(groups);

        List<Group> result = groupService.getAllGroups();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroup.getName(), result.get(0).getName());
    }

    @Test
    void getGroupById_WhenGroupExists_ShouldReturnGroup() {
        when(groupRepository.findById(1)).thenReturn(Optional.of(testGroup));

        Group result = groupService.getGroupById(1);

        assertNotNull(result);
        assertEquals(testGroup.getName(), result.getName());
    }

    @Test
    void getGroupById_WhenGroupDoesNotExist_ShouldReturnNull() {
        when(groupRepository.findById(1)).thenReturn(Optional.empty());

        Group result = groupService.getGroupById(1);

        assertNull(result);
    }

    @Test
    void getGroupsByName_ShouldReturnMatchingGroups() {
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findByNameContainingIgnoreCase("Test")).thenReturn(groups);

        List<Group> result = groupService.getGroupsByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroup.getName(), result.get(0).getName());
    }

    @Test
    void createGroup_ShouldReturnSavedGroup() {
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.createGroup(testGroup);

        assertNotNull(result);
        assertEquals(testGroup.getName(), result.getName());
    }

    @Test
    void updateGroup_ShouldReturnUpdatedGroup() {
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.updateGroup(testGroup);

        assertNotNull(result);
        assertEquals(testGroup.getName(), result.getName());
    }

    @Test
    void deleteGroup_ShouldCallRepositoryDelete() {
        doNothing().when(groupRepository).deleteById(1);

        groupService.deleteGroup(1);

        verify(groupRepository, times(1)).deleteById(1);
    }
} 