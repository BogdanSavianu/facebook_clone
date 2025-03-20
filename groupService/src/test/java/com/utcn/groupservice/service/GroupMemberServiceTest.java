package com.utcn.groupservice.service;

import com.utcn.groupservice.entity.Group;
import com.utcn.groupservice.entity.GroupMember;
import com.utcn.groupservice.entity.GroupMember.GroupMemberId;
import com.utcn.groupservice.repository.GroupMemberRepository;
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
public class GroupMemberServiceTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupMemberService groupMemberService;

    private GroupMember testGroupMember;
    private GroupMemberId testGroupMemberId;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new Group();
        testGroup.setId(1);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setPrivacySetting("PUBLIC");

        testGroupMemberId = new GroupMemberId(1, 1);
        testGroupMember = new GroupMember();
        testGroupMember.setId(testGroupMemberId);
        testGroupMember.setGroup(testGroup);
        testGroupMember.setUserId(1);
    }

    @Test
    void getAllGroupMembers_ShouldReturnListOfMembers() {
        List<GroupMember> members = Arrays.asList(testGroupMember);
        when(groupMemberRepository.findAll()).thenReturn(members);

        List<GroupMember> result = groupMemberService.getAllGroupMembers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroupMember.getUserId(), result.get(0).getUserId());
    }

    @Test
    void getGroupMemberById_WhenMemberExists_ShouldReturnMember() {
        when(groupMemberRepository.findById(testGroupMemberId)).thenReturn(Optional.of(testGroupMember));

        GroupMember result = groupMemberService.getGroupMemberById(testGroupMemberId);

        assertNotNull(result);
        assertEquals(testGroupMember.getUserId(), result.getUserId());
    }

    @Test
    void getGroupMemberById_WhenMemberDoesNotExist_ShouldReturnNull() {
        when(groupMemberRepository.findById(testGroupMemberId)).thenReturn(Optional.empty());

        GroupMember result = groupMemberService.getGroupMemberById(testGroupMemberId);

        assertNull(result);
    }

    @Test
    void getGroupMembersByGroupId_ShouldReturnMatchingMembers() {
        List<GroupMember> members = Arrays.asList(testGroupMember);
        when(groupMemberRepository.findByIdGroupId(1)).thenReturn(members);

        List<GroupMember> result = groupMemberService.getGroupMembersByGroupId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroupMember.getUserId(), result.get(0).getUserId());
    }

    @Test
    void getGroupMembersByUserId_ShouldReturnMatchingMembers() {
        List<GroupMember> members = Arrays.asList(testGroupMember);
        when(groupMemberRepository.findByIdUserId(1)).thenReturn(members);

        List<GroupMember> result = groupMemberService.getGroupMembersByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroupMember.getUserId(), result.get(0).getUserId());
    }

    @Test
    void addGroupMember_ShouldReturnSavedMember() {
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(testGroupMember);

        GroupMember result = groupMemberService.addGroupMember(testGroupMember);

        assertNotNull(result);
        assertEquals(testGroupMember.getUserId(), result.getUserId());
    }

    @Test
    void updateGroupMember_ShouldReturnUpdatedMember() {
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(testGroupMember);

        GroupMember result = groupMemberService.updateGroupMember(testGroupMember);

        assertNotNull(result);
        assertEquals(testGroupMember.getUserId(), result.getUserId());
    }

    @Test
    void deleteGroupMember_ShouldCallRepositoryDelete() {
        doNothing().when(groupMemberRepository).deleteById(testGroupMemberId);

        groupMemberService.deleteGroupMember(testGroupMemberId);
        verify(groupMemberRepository, times(1)).deleteById(testGroupMemberId);
    }
} 