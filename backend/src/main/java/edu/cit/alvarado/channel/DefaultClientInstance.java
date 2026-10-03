package edu.cit.alvarado.channel;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
final class DefaultClientInstance implements ClientInstance {
    private final String id = UUID.randomUUID().toString();
    private final OffsetDateTime startedAt = OffsetDateTime.now(ZoneOffset.UTC);

    @Override
    public String id() { return id; }

    @Override
    public OffsetDateTime startedAt() { return startedAt; }
}
