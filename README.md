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
