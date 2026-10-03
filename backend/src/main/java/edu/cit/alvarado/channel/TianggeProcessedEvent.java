package edu.cit.alvarado.channel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tiangge_processed_events")
public class TianggeProcessedEvent {
    @Id
    private String eventId;
    private long seq;
    private OffsetDateTime processedAt;

    protected TianggeProcessedEvent() {}
    public TianggeProcessedEvent(String eventId, long seq) {
        this.eventId = eventId;
        this.seq = seq;
        this.processedAt = OffsetDateTime.now();
    }
    public String getEventId() { return eventId; }
    public long getSeq() { return seq; }
}
