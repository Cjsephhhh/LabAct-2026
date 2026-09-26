package edu.cit.alvarado.supplier.adapter;

import edu.cit.alvarado.supplier.SupplierGateway;
import edu.cit.alvarado.supplier.SupplierOrderResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
class LegacySupplyGateway implements SupplierGateway {

    private final LegacySupplyClient client;
    private final LegacySupplySession session;
    private final LegacySupplyTranslator translator;

    private final String clientId;
    private final String apiKey;

    LegacySupplyGateway(
            LegacySupplyClient client,
            LegacySupplySession session,
            LegacySupplyTranslator translator
    ) {
        this.client = client;
        this.session = session;
        this.translator = translator;

        this.clientId = requiredEnvironmentVariable("LS_CLIENT_ID");
        this.apiKey = requiredEnvironmentVariable("LS_API_KEY");
    }

    @Override
    public SupplierOrderResult placeOrder(
        String productId,
        int unitsNeeded,
        String buyerRef,
        String requestId
    )
    
    {

        if (unitsNeeded <= 0) {
            throw new IllegalArgumentException(
                    "Units needed must be greater than zero"
            );
        }

        if (buyerRef == null || buyerRef.isBlank()) {
            throw new IllegalArgumentException(
                    "BuyerRef must not be blank"
            );
        }

        int packSize = packSizeFor(productId);

        int cases = (int) Math.ceil(
                (double) unitsNeeded / packSize
        );

        SupplierOrderResult existing =
                findExistingOrder(productId, buyerRef);

        if (existing != null) {
            return existing;
        }

        String response;

        try {
            response = executePurchaseOrder(
                    requestId,
                    productId,
                    cases,
                    buyerRef
            );

        } catch (RestClientResponseException exception) {

            if (isExpiredSession(exception)) {

                session.clear();
                authenticate();

                response = executePurchaseOrder(
                        requestId,
                        productId,
                        cases,
                        buyerRef
                );

            } else {
                throw exception;
            }
        }

        return translator.translatePurchaseOrder(
                response,
                productId
        );
    }

    @Override
    public SupplierOrderResult getOrderStatus(
            String buyerRef
    ) {

        if (buyerRef == null || buyerRef.isBlank()) {
            throw new IllegalArgumentException(
                    "BuyerRef must not be blank"
            );
        }
        
        String response = executeWithSessionRetry(
                () -> client.findPurchaseOrderByBuyerRef(
                        session.getSessionToken(),
                        buyerRef
                )
        );

        return translateLookupResponse(response);
    }

    private String executePurchaseOrder(
            String requestId,
            String productId,
            int cases,
            String buyerRef
    ) {

        String supplierSku = supplierSkuFor(productId);

        return executeWithSessionRetry(
                () -> client.createPurchaseOrder(
                        session.getSessionToken(),
                        requestId,
                        supplierSku,
                        cases,
                        buyerRef
                )
        );
    }

    private String executeWithSessionRetry(
        SupplierCall call
) {

    ensureAuthenticated();

    RestClientResponseException lastException = null;

    for (int attempt = 1; attempt <= 3; attempt++) {

        try {
            return call.execute();

        } catch (RestClientResponseException exception) {

            lastException = exception;

            if (!isRetryable(exception)) {
                throw exception;
            }

            if (isExpiredSession(exception)) {
                session.clear();
                authenticate();
            }

            if (attempt < 3) {
                sleepBeforeRetry(attempt);
            }
        }
    }

    throw lastException;
}

    private boolean isRetryable(
        RestClientResponseException exception
) {

    int status = exception.getStatusCode().value();

    return status == 401
            || status == 429
            || status == 503;
}

    private void sleepBeforeRetry(int attempt) {

    try {

        long delayMillis = switch (attempt) {
            case 1 -> 250;
            case 2 -> 500;
            default -> 0;
        };

        if (delayMillis > 0) {
            Thread.sleep(delayMillis);
        }

    } catch (InterruptedException exception) {

        Thread.currentThread().interrupt();

        throw new IllegalStateException(
                "Retry interrupted",
                exception
        );
    }
}

    private void ensureAuthenticated() {

        if (!session.hasSession()) {
            authenticate();
        }
    }

    private void authenticate() {

        String response = client.authenticate(
                clientId,
                apiKey
        );

        String token = translator.sessionToken(response);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "LegacySupply authentication returned no session token"
            );
        }

        session.setSessionToken(token);
    }

    private SupplierOrderResult findExistingOrder(
            String productId,
            String buyerRef
    ) {

        try {

            String response =
                    executeWithSessionRetry(
                            () -> client.findPurchaseOrderByBuyerRef(
                                    session.getSessionToken(),
                                    buyerRef
                            )
                    );

            return translator.translatePurchaseOrderList(
                    response,
                    productId
            );

        } catch (RestClientResponseException exception) {

            /*
             * A missing BuyerRef result is not an application failure.
             * LegacySupply may return a query error or empty result.
             */
            if (exception.getStatusCode().value() == 400) {
                return null;
            }

            throw exception;
        }
    }

    private SupplierOrderResult translateLookupResponse(
            String response
    ) {

        String supplierSku =
                extractSupplierSku(response);

        String productId =
                productIdForSupplierSku(supplierSku);

        return translator.translatePurchaseOrderList(
                response,
                productId
        );
    }

    private String extractSupplierSku(String xml) {

        return xml
                .replaceAll(
                        "(?s).*<SupplierSku>(.*?)</SupplierSku>.*",
                        "$1"
                )
                .trim();
    }

    private boolean isExpiredSession(
            RestClientResponseException exception
    ) {

        String body = exception.getResponseBodyAsString();

        return exception.getStatusCode().value() == 401
                && body.contains("E-AUTH-07");
    }

    private int packSizeFor(String productId) {

        return switch (productId) {
            case "P100" -> 24;
            case "P200" -> 20;
            case "P300" -> 12;

            default -> throw new IllegalArgumentException(
                    "No supplier pack size configured for product: "
                            + productId
            );
        };
    }

    private String supplierSkuFor(String productId) {

        return switch (productId) {
            case "P100" -> "YQP-1135";
            case "P200" -> "YQP-4388";
            case "P300" -> "YQP-6798";

            default -> throw new IllegalArgumentException(
                    "No LegacySupply SKU configured for product: "
                            + productId
            );
        };
    }

    private String productIdForSupplierSku(
            String supplierSku
    ) {

        return switch (supplierSku) {
            case "YQP-1135" -> "P100";
            case "YQP-4388" -> "P200";
            case "YQP-6798" -> "P300";

            default -> throw new IllegalArgumentException(
                    "Unknown LegacySupply SKU: "
                            + supplierSku
            );
        };
    }

    private static String requiredEnvironmentVariable(
            String name
    ) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable is missing: "
                            + name
            );
        }

        return value;
    }

    @FunctionalInterface
    private interface SupplierCall {

        String execute();
    }
}