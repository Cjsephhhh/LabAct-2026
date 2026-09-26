# IT342 System Integration — Extending the Modular Monolith

Spring Boot + React + Supabase PostgreSQL modular monolith.

## Modules
- `edu.cit.alvarado.shop` — Order module
- `edu.cit.alvarado.inventory` — Inventory module
- `edu.cit.alvarado.notification` — Notification module

## Features
- Multi-item orders with all-or-nothing transactional reservation.
- Order cancellation with inventory restock.
- `GET /api/inventory` and `GET /api/orders`.
- Spring application events for confirmed/rejected orders.
- Low-stock `Order needed` notifications.
- Package-private `InventoryServiceImpl`.

## Database
Run `supabase.sql` in Supabase SQL Editor. It recreates `inventory`, `orders`, `order_items`, and `notifications` and seeds P100/P200/P300.

## Environment
Never commit the real database password. Set these in the terminal:
```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME="postgres.YOUR_PROJECT_REF"
$env:SUPABASE_DB_PASSWORD="YOUR_DATABASE_PASSWORD"
```

## Run
Backend:
```powershell
cd backend
mvn spring-boot:run
```
Frontend in a second terminal:
```powershell
cd frontend
npm install
npm run dev
```

## API
`POST /api/orders`
```json
{"items":[{"productId":"P100","quantity":2},{"productId":"P200","quantity":1}]}
```
`POST /api/orders/{orderId}/cancel`
`GET /api/inventory`
`GET /api/orders`
`GET /api/notifications`

## Atomicity
Order validates every requested item before changing stock. The surrounding Spring `@Transactional` boundary covers the reservation loop and order persistence, while inventory rows are pessimistically locked. If Order and Inventory became microservices, a saga or compensating transaction approach would be needed because one local transaction could no longer span both services.

## Event-driven notification
Order publishes `OrderPlacedEvent` and `OrderRejectedEvent` using `ApplicationEventPublisher`; Notification listens with `@EventListener` and never calls OrderService or InventoryService. Inventory publishes `LowStockEvent` after a successful reservation crosses the threshold. If Notification became a microservice, a broker, retries, idempotency, dead-letter handling, and event schema/versioning would become relevant.

## Reflection
Inventory is a reasonable first extraction candidate because it has a clear service contract and owns inventory state. Extraction would replace the in-process interface call with a network API or message contract, move Inventory to its own deployable application, establish database ownership, and add authentication, timeouts, retries, observability, and distributed consistency handling.

## Evidence checklist
Capture Network-tab evidence for a successful multi-item order, rejected multi-item order with no partial reservation, cancellation/restock, notification activity, and low-stock alert. Put screenshots in `docs/`.





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

