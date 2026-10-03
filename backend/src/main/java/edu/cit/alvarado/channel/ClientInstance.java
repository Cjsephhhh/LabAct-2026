package edu.cit.alvarado.channel;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Identifies this running application instance.
 * A new UUID is generated each time the Spring application starts.
 */
@Component
public final class ClientInstance {

    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}
