package com.utcn.groupservice.controller;

import com.utcn.groupservice.entity.GroupMember;
import com.utcn.groupservice.entity.GroupMember.GroupMemberId;
import com.utcn.groupservice.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group-members")
public class GroupMemberController {

    @Autowired
    private GroupMemberService groupMemberService;

    @GetMapping
    public ResponseEntity<List<GroupMember>> getAllGroupMembers() {
        return ResponseEntity.ok(groupMemberService.getAllGroupMembers());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<GroupMember>> getGroupMembersByGroupId(@PathVariable Integer groupId) {
        return ResponseEntity.ok(groupMemberService.getGroupMembersByGroupId(groupId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupMember>> getGroupMembersByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(groupMemberService.getGroupMembersByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<GroupMember> createGroupMember(@RequestBody GroupMember groupMember) {
        return new ResponseEntity<>(groupMemberService.addGroupMember(groupMember), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<GroupMember> updateGroupMember(@RequestBody GroupMember groupMember) {
        GroupMember existingMember = groupMemberService.getGroupMemberById(groupMember.getId());
        if (existingMember == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupMemberService.updateGroupMember(groupMember));
    }

    @DeleteMapping("/{groupId}/{userId}")
    public ResponseEntity<Void> deleteGroupMember(@PathVariable Integer groupId, @PathVariable Integer userId) {
        GroupMemberId id = new GroupMemberId(groupId, userId);
        GroupMember existingMember = groupMemberService.getGroupMemberById(id);
        if (existingMember == null) {
            return ResponseEntity.notFound().build();
        }
        groupMemberService.deleteGroupMember(id);
        return ResponseEntity.noContent().build();
    }
} 