package com.utcn.groupservice.repository;

import com.utcn.groupservice.entity.GroupMember;
import com.utcn.groupservice.entity.GroupMember.GroupMemberId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends CrudRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findByIdGroupId(Integer groupId);
    List<GroupMember> findByIdUserId(Integer userId);
} 