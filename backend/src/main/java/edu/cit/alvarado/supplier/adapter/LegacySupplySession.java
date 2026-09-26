package edu.cit.alvarado.supplier.adapter;

import org.springframework.stereotype.Component;

@Component
class LegacySupplySession {

    private String sessionToken;

    String getSessionToken() {
        return sessionToken;
    }

    void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    void clear() {
        this.sessionToken = null;
    }

    boolean hasSession() {
        return sessionToken != null && !sessionToken.isBlank();
    }
}