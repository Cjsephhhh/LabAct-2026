package edu.cit.alvarado.channel;

import java.time.OffsetDateTime;

public interface ClientInstance {
    String id();
    OffsetDateTime startedAt();
}
