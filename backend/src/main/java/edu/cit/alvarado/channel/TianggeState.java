package edu.cit.alvarado.channel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tiangge_state")
public class TianggeState {
    @Id
    private Long id;
    private long cursor;

    protected TianggeState() {}
    public TianggeState(Long id, long cursor) { this.id = id; this.cursor = cursor; }
    public Long getId() { return id; }
    public long getCursor() { return cursor; }
    public void setCursor(long cursor) { this.cursor = cursor; }
}
