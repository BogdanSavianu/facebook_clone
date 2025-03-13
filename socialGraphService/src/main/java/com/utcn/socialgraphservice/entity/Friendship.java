package com.utcn.socialgraphservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "friendships")
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @Column(name = "id_requester", insertable = false, updatable = false)
    private Integer requesterId;

    @Column(name = "id_addressee", insertable = false, updatable = false)
    private Integer addresseeId;

    @Column
    private String status;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Embeddable
    public static class FriendshipId implements Serializable {

        @Column(name = "id_requester")
        private Integer idRequester;

        @Column(name = "id_addressee")
        private Integer idAddressee;

        public FriendshipId() {
        }

        public FriendshipId(Integer idRequester, Integer idAddressee) {
            this.idRequester = idRequester;
            this.idAddressee = idAddressee;
        }

        public Integer getIdRequester() {
            return idRequester;
        }

        public void setIdRequester(Integer idRequester) {
            this.idRequester = idRequester;
        }

        public Integer getIdAddressee() {
            return idAddressee;
        }

        public void setIdAddressee(Integer idAddressee) {
            this.idAddressee = idAddressee;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FriendshipId that = (FriendshipId) o;
            return Objects.equals(idRequester, that.idRequester) &&
                    Objects.equals(idAddressee, that.idAddressee);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idRequester, idAddressee);
        }
    }
} 