# LegacySupply Integration

## Product Mapping

The following mappings were obtained from the LegacySupply catalog
endpoint using an authenticated session.

| Product ID | Product Name | SupplierSku | PackSize |
|---|---|---|---:|
| P100 | Wireless Mouse | YQP-1135 | 24 |
| P200 | Mechanical Keyboard | YQP-4388 | 20 |
| P300 | USB-C Hub | YQP-6798 | 12 |

The SupplierSku and PackSize values belong to the LegacySupply system.
They are kept inside the supplier integration layer and are not exposed
to the Order or Inventory domain.

---

## Purchase Order Tests

Three purchase orders were successfully created through the
LegacySupply API during manual integration testing.

### Order 1

| Field | Observed Value |
|---|---|
| SupplierSku | YQP-1135 |
| Qty | 1 |
| Uom | CS |
| BuyerRef | LAB3-TEST-001 |
| PO Number | PO-100054 |
| StatusCode | 10 |
| HTTP Status | 201 Created |

The Wireless Mouse has a PackSize of 24. Therefore, Qty 1 with Uom CS
represents one case containing 24 individual units.

### Order 2

| Field | Observed Value |
|---|---|
| SupplierSku | [ACTUAL SKU] |
| Qty | [ACTUAL QTY] |
| Uom | [ACTUAL UOM] |
| BuyerRef | LAB3-TEST-002 |
| PO Number | [ACTUAL PO NUMBER] |
| StatusCode | [ACTUAL STATUS] |
| HTTP Status | 201 Created |

### Order 3

| Field | Observed Value |
|---|---|
| SupplierSku | [ACTUAL SKU] |
| Qty | [ACTUAL QTY] |
| Uom | [ACTUAL UOM] |
| BuyerRef | LAB3-TEST-003 |
| PO Number | [ACTUAL PO NUMBER] |
| StatusCode | [ACTUAL STATUS] |
| HTTP Status | 201 Created |

The three purchase orders were created using different BuyerRef and
X-Request-Id values to identify each integration request separately.

---

## Purchase Order Tracking

The purchase order PO-100054 was initially accepted with StatusCode 10.
A later tracking request returned StatusCode 40, meaning the order was
delivered.

The observed tracking response was:

| Field | Observed Value |
|---|---|
| PO Number | PO-100054 |
| StatusCode | 40 |
| SupplierSku | YQP-1135 |
| Qty | 1 |
| Uom | CS |
| BuyerRef | LAB3-TEST-001 |

The following LegacySupply status codes were documented during API
discovery:

| StatusCode | LegacySupply Meaning | Our Meaning |
|---:|---|---|
| 10 | Accepted | ACCEPTED |
| 20 | Picking | PICKING |
| 30 | Shipped | SHIPPED |
| 40 | Delivered | DELIVERED |

---

## BuyerRef Lookup

LegacySupply supports looking up purchase orders using BuyerRef.

A lookup using `LAB3-TEST-001` returned one purchase order:

| Field | Observed Value |
|---|---|
| Count | 1 |
| PO Number | PO-100054 |
| StatusCode | 40 |
| SupplierSku | YQP-1135 |
| Qty | 1 |
| Uom | CS |
| BuyerRef | LAB3-TEST-001 |

BuyerRef lookup can be used by the adapter when checking whether a
reorder has already been submitted before retrying a failed request.

This provides an additional way to verify whether an order was already
created and helps prevent duplicate purchase orders.

---

## Observed Errors

The following errors were actually observed during manual API testing
with Postman.

| Error Code | HTTP Status | Cause |
|---|---:|---|
| E-AUTH-07 | 401 | The LegacySupply session was no longer valid when an authenticated request was made. |
| E-SYS-99 | 503 | LegacySupply was temporarily unavailable while requesting a session. |

These errors were encountered during testing rather than being added
only from the API documentation.

---

## Session

LegacySupply uses a short-lived session-based authentication mechanism.

The integration first sends the student ID and API key to the
`/auth/token` endpoint. LegacySupply returns a session token. The token
is then sent with authenticated requests using the `X-LS-Session`
header.

When the session expires, LegacySupply returns `E-AUTH-07`. The
integration must then obtain a new session before continuing the
request.

The self-check also confirmed that expired sessions were encountered
and renewed during testing.

### Measured Session Lifetime

Session issued at:

`[ENTER ACTUAL ISSUE DATE AND TIME]`

Session expired at:

`[ENTER ACTUAL EXPIRATION DATE AND TIME]`

Measured session lifetime:

`[ENTER ACTUAL DURATION]`

The measured value will be used when implementing session management in
the LegacySupply adapter.

---

## X-Request-Id and Idempotency

LegacySupply uses `X-Request-Id` to identify integration requests.

Each purchase-order request was given a unique request ID. The same
request ID must be reused when retrying the same logical operation so
that a retry does not accidentally create a second purchase order.

The request ID must not be changed between retries of the same
operation.

LegacySupply also provides an error when the same request ID is reused
with different request content. This helps prevent accidental reuse of
an idempotency key for another purchase order.

---

## Qty and Uom

`Qty` is the quantity sent to LegacySupply, while `Uom` identifies the
supplier's unit of measure. In the observed purchase orders, the Uom
was `CS`, representing cases.

The catalog `PackSize` specifies how many individual inventory units
are contained in one case.

For example, the Wireless Mouse has a PackSize of 24. A request with:

Qty = 1  
Uom = CS

represents one case containing:

1 × 24 = 24 individual units

If the application needs 25 individual units, the adapter cannot
request 25 individual units when ordering in cases. It must round the
required quantity up to the next whole case:

25 / 24 = 1.0417 cases

Rounding up gives:

2 cases

The resulting number of individual units is:

2 × 24 = 48 units

Therefore, a requirement for 25 Wireless Mouse units would result in a
supplier request for 2 cases, providing 48 units.

---

## API Endpoints Discovered

The following LegacySupply endpoints were used during manual API
discovery:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/token` | Obtain a LegacySupply session |
| GET | `/ping` | Check service availability |
| GET | `/catalog` | Retrieve supplier catalog |
| POST | `/purchase-orders` | Create a purchase order |
| GET | `/purchase-orders/{PoNumber}` | Track a purchase order |
| GET | `/purchase-orders?buyerRef={BuyerRef}` | Find orders by BuyerRef |

Authenticated requests use the `X-LS-Session` header.

Purchase-order XML requests use:

`Content-Type: application/xml`

---

## Integration Notes

The LegacySupply API is XML-based, while the application uses its own
domain models. LegacySupply-specific concepts such as SupplierSku,
PackSize, supplier status codes, XML request/response objects, session
tokens, and HTTP communication will therefore remain inside the
supplier integration layer.

The Order and Inventory modules should communicate with the supplier
through the application's own `SupplierGateway` interface rather than
calling LegacySupply directly.

# Reflection

## 1. Unexpected StatusCode 90

PO-100217 with BuyerRef `LAB3-TEST-003` returned StatusCode 90, which was
not included in the documented LegacySupply status codes. I treated this
as an unexpected terminal status because the order did not represent a
normal Accepted, Picking, Shipped, or Delivered state. My system should
not assume that an unknown status means Delivered, so the adapter will
map it to an `UNKNOWN` or failure-related status in our own enum and keep
the order from adding stock. This prevents inventory from being increased
when the supplier did not actually deliver the expected items.

## 2. LegacySupply Session Lifetime

LegacySupply does not provide the exact session lifetime, so I measured
it by recording when a session was issued and when an authenticated
request using that session returned `E-AUTH-07`. The measured session
lifetime was **[ENTER YOUR ACTUAL SESSION LIFETIME]**. My adapter will
keep the session internally and use it for authenticated requests until
LegacySupply rejects it as expired. When an expired session is detected,
the adapter will obtain a new session and retry the same operation using
the same `X-Request-Id`.

## 3. PackSize, Qty, and Delivered Units

For my Wireless Mouse order `PO-100054`, LegacySupply returned `Qty = 1`
and `Uom = CS`, while the catalog showed a PackSize of 24. Therefore,
1 case × 24 units per case = 24 individual units delivered. If the
Inventory module needs 25 individual units, the adapter must round
25 ÷ 24 = 1.0417 cases up to 2 cases. The supplier request would therefore
send `Qty = 2`, resulting in 2 × 24 = 48 individual units when delivered.
