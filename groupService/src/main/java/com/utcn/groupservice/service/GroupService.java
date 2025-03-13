package com.utcn.groupservice.service;

import com.utcn.groupservice.entity.Group;
import com.utcn.groupservice.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAll().forEach(groups::add);
        return groups;
    }

    public Group getGroupById(Integer id) {
        Optional<Group> group = groupRepository.findById(id);
        return group.orElse(null);
    }

    public List<Group> getGroupsByName(String name) {
        return groupRepository.findByName(name);
    }

    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    public Group updateGroup(Group group) {
        return groupRepository.save(group);
    }

    public void deleteGroup(Integer id) {
        groupRepository.deleteById(id);
    }
} 