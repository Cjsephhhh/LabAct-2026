package edu.cit.alvarado.channel;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Component
final class TianggeClient implements MarketplaceGateway {
    private final RestClient client;
    private final java.time.OffsetDateTime instanceStartedAt;

    TianggeClient(ClientInstance instance) {
        this.instanceStartedAt = instance.startedAt();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        String baseUrl = System.getenv().getOrDefault(
                "TIANGGE_BASE_URL",
                "https://legacysupply.onrender.com/tiangge/v1"
        );
        String clientId = required("TIANGGE_CLIENT_ID", "LS_CLIENT_ID");
        String apiKey = required("TIANGGE_API_KEY", "LS_API_KEY");

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Client-Id", clientId)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().set("X-Client-Instance", instance.id());
                    return execution.execute(request, body);
                })
                .build();
    }

    @Override
    public void heartbeat(long uptimeSeconds) {
        retry(() -> client.post().uri("/instances/heartbeat")
                .body(new HeartbeatBody("lab4-shop", instanceStartedAt.toString(), uptimeSeconds))
                .retrieve().toBodilessEntity());
    }

    @Override
    public void publishListings(List<TianggeListing> listings) {
        retry(() -> client.put().uri("/listings").body(listings).retrieve().toBodilessEntity());
    }

    @Override
    public void publishStock(List<TianggeStock> stock) {
        retry(() -> client.put().uri("/stock").body(stock).retrieve().toBodilessEntity());
    }

    @Override
    public void decide(String orderId, TianggeDecision decision) {
        retry(() -> client.post().uri("/orders/{orderId}/decision", orderId)
                .body(decision).retrieve().toBodilessEntity());
    }

    @Override
    public void resolve(String orderId, String status) {
        retry(() -> client.post().uri("/orders/{orderId}/resolution", orderId)
                .body(new ResolutionBody(status)).retrieve().toBodilessEntity());
    }

    @Override
    public void confirmCancellation(String orderId, boolean restocked) {
        retry(() -> client.post().uri("/orders/{orderId}/cancellation", orderId)
                .body(new CancellationBody(restocked)).retrieve().toBodilessEntity());
    }

    TianggeFeed feed(long cursor) {
        return retry(() -> client.get().uri(uriBuilder -> uriBuilder
                        .path("/feed")
                        .queryParam("after", cursor)
                        .queryParam("limit", 50)
                        .build())
                .retrieve().body(TianggeFeed.class));
    }

    private <T> T retry(Supplier<T> action) {
        RestClientResponseException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return action.get();
            } catch (RestClientResponseException ex) {
                last = ex;
                int status = ex.getStatusCode().value();
                if (status != 503 && status != 429) throw ex;
                try {
                    Thread.sleep(attempt * 300L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
        }
        throw last;
    }

    private static String required(String primary, String fallback) {
        String value = System.getenv(primary);
        if (value == null || value.isBlank()) value = System.getenv(fallback);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing environment variable: " + primary);
        return value;
    }

    record HeartbeatBody(String appName, String startedAt, long uptimeSeconds) {}
    record ResolutionBody(String status) {}
    record CancellationBody(boolean restocked) {}
    static class TianggeFeed {
        public List<TianggeEvent> events = List.of();
        public long nextCursor;
    }
    static class TianggeEvent {
        public long seq;
        public String eventId;
        public String type;
        public String orderId;
        public String placedAt;
        public String cancelledAt;
        public String decisionDeadline;
        public String confirmDeadline;
        public List<TianggeLine> lines = List.of();
    }
    static class TianggeLine {
        public String sellerSku;
        public int qty;
    }
}
