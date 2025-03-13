package com.utcn.groupservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "group_members")
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Integer userId;

    @Embeddable
    public static class GroupMemberId implements Serializable {

        @Column(name = "group_id")
        private Integer groupId;

        @Column(name = "user_id")
        private Integer userId;

        public GroupMemberId() {
        }

        public GroupMemberId(Integer groupId, Integer userId) {
            this.groupId = groupId;
            this.userId = userId;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public void setGroupId(Integer groupId) {
            this.groupId = groupId;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupMemberId that = (GroupMemberId) o;
            return Objects.equals(groupId, that.groupId) &&
                    Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, userId);
        }
    }
} 