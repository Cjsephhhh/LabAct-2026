package edu.cit.alvarado.channel;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class ClientInstance {

    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}
