package com.utcn.contentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "post_tags")
public class PostTag {

    @EmbeddedId
    private PostTagId id;

    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Postable post;

    @Column(name = "tagged_person_id", insertable = false, updatable = false)
    private Integer taggedPersonId;

    @Setter
    @Getter
    @Embeddable
    public static class PostTagId implements Serializable {

        @Column(name = "post_id")
        private Integer postId;

        @Column(name = "tagged_person_id")
        private Integer taggedPersonId;

        public PostTagId() {
        }

        public PostTagId(Integer postId, Integer taggedPersonId) {
            this.postId = postId;
            this.taggedPersonId = taggedPersonId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostTagId that = (PostTagId) o;
            return Objects.equals(postId, that.postId) &&
                    Objects.equals(taggedPersonId, that.taggedPersonId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, taggedPersonId);
        }
    }
} 