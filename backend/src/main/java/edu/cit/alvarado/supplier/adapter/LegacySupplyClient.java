package edu.cit.alvarado.supplier.adapter;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class LegacySupplyClient {

    private final RestClient restClient;

    LegacySupplyClient(RestClient restClient) {
        this.restClient = restClient;
    }

    String authenticate(String clientId, String apiKey) {

        String xml = """
                <AuthRequest>
                    <ClientId>%s</ClientId>
                    <ApiKey>%s</ApiKey>
                </AuthRequest>
                """.formatted(clientId, apiKey);

        return restClient.post()
                .uri("/auth/token")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml)
                .retrieve()
                .body(String.class);
    }

    String getCatalog(String sessionToken) {

        return restClient.get()
                .uri("/catalog")
                .header("X-LS-Session", sessionToken)
                .retrieve()
                .body(String.class);
    }

    String createPurchaseOrder(
            String sessionToken,
            String requestId,
            String supplierSku,
            int quantity,
            String buyerRef
    ) {

        String xml = """
                <PurchaseOrder>
                    <SupplierSku>%s</SupplierSku>
                    <Qty>%d</Qty>
                    <BuyerRef>%s</BuyerRef>
                </PurchaseOrder>
                """.formatted(
                supplierSku,
                quantity,
                buyerRef
        );

        return restClient.post()
                .uri("/purchase-orders")
                .header("X-LS-Session", sessionToken)
                .header("X-Request-Id", requestId)
                .contentType(MediaType.APPLICATION_XML)
                .body(xml)
                .retrieve()
                .body(String.class);
    }

    String getPurchaseOrder(
            String sessionToken,
            String poNumber
    ) {

        return restClient.get()
                .uri("/purchase-orders/{poNumber}", poNumber)
                .header("X-LS-Session", sessionToken)
                .retrieve()
                .body(String.class);
    }

    String findPurchaseOrderByBuyerRef(
            String sessionToken,
            String buyerRef
    ) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/purchase-orders")
                        .queryParam("buyerRef", buyerRef)
                        .build())
                .header("X-LS-Session", sessionToken)
                .retrieve()
                .body(String.class);
    }
}