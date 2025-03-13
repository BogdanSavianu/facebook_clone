package com.utcn.eventservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "event_attendees")
public class EventAttendee {

    @EmbeddedId
    private EventAttendeeId id;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Integer memberId;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @Setter
    @Getter
    @Embeddable
    public static class EventAttendeeId implements Serializable {

        @Column(name = "member_id")
        private Integer memberId;

        @Column(name = "event_id")
        private Integer eventId;

        public EventAttendeeId() {
        }

        public EventAttendeeId(Integer memberId, Integer eventId) {
            this.memberId = memberId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventAttendeeId that = (EventAttendeeId) o;
            return Objects.equals(memberId, that.memberId) &&
                    Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(memberId, eventId);
        }
    }
} 