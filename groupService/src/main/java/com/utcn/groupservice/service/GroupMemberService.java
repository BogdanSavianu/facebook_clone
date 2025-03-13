package com.utcn.groupservice.service;

import com.utcn.groupservice.entity.GroupMember;
import com.utcn.groupservice.entity.GroupMember.GroupMemberId;
import com.utcn.groupservice.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GroupMemberService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public List<GroupMember> getAllGroupMembers() {
        List<GroupMember> members = new ArrayList<>();
        groupMemberRepository.findAll().forEach(members::add);
        return members;
    }

    public GroupMember getGroupMemberById(GroupMemberId id) {
        Optional<GroupMember> member = groupMemberRepository.findById(id);
        return member.orElse(null);
    }

    public List<GroupMember> getGroupMembersByGroupId(Integer groupId) {
        return groupMemberRepository.findByIdGroupId(groupId);
    }

    public List<GroupMember> getGroupMembersByUserId(Integer userId) {
        return groupMemberRepository.findByIdUserId(userId);
    }

    public GroupMember addGroupMember(GroupMember member) {
        return groupMemberRepository.save(member);
    }

    public GroupMember updateGroupMember(GroupMember member) {
        return groupMemberRepository.save(member);
    }

    public void deleteGroupMember(GroupMemberId id) {
        groupMemberRepository.deleteById(id);
    }
} 