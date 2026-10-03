# IT342 System Integration
## Lab 4 — Tiangge Marketplace: Run Your Shop Unattended

This project extends the modular monolith from the previous laboratory by connecting it to the Tiangge marketplace and LegacySupply supplier.

The Spring Boot application is responsible for communicating with Tiangge and LegacySupply. Once the application is running, it works without manual requests from Postman or a browser.

## 1. Project Overview

The application is a modular monolith built with:

- Spring Boot
- Java
- React
- PostgreSQL / Supabase
- Tiangge Seller API
- LegacySupply Supplier API

The main goal of Lab 4 is to make the shop operate automatically. The application publishes products and stock, receives marketplace orders through the Tiangge feed, processes orders using the existing Order and Inventory modules, handles cancellations, and restocks products through LegacySupply.

## 2. Module Structure

The backend is organized into separate modules:

| Module | Responsibility |
|---|---|
| `edu.cit.alvarado.shop` | Creates and manages shop orders |
| `edu.cit.alvarado.inventory` | Manages product stock and inventory operations |
| `edu.cit.alvarado.notification` | Handles application/domain events |
| `edu.cit.alvarado.supplier` | Communicates with LegacySupply |
| `edu.cit.alvarado.channel` | Handles Tiangge marketplace integration |

The Order and Inventory modules do not directly depend on Tiangge. Marketplace events enter through the channel module and are passed to the existing application services.

## 3. Tiangge Integration

The Tiangge API is available under:

`https://legacysupply.onrender.com/tiangge/v1`

The application uses the following main operations:

- `POST /instances/heartbeat` — identifies the running application
- `PUT /listings` — publishes products for sale
- `PUT /stock` — publishes available inventory
- `GET /feed` — checks for new orders and cancellations
- `POST /orders/{orderId}/decision` — sends an order decision
- `POST /orders/{orderId}/resolution` — resolves a backorder
- `POST /orders/{orderId}/cancellation` — confirms a customer cancellation

All Tiangge requests use the required client ID, API key, and application instance UUID.

## 4. Application Instance

Every time Spring Boot starts, the application generates a new UUID.

The UUID is used as the value of:

`X-Client-Instance`

The same instance ID is used for all external requests while that application instance is running.

The application also sends a heartbeat every 30 seconds so Tiangge knows that the shop is online.

A new UUID should be generated after every application restart. The instance ID from the application log can be compared with the instance shown by the Tiangge self-check page.

## 5. Product and Supplier Mapping

The three products used for the marketplace are:

| Product | Seller SKU | LegacySupply SupplierSku | Pack Size |
|---|---|---|---:|
| Wireless Mouse | P100 | YQP-1135 | 24 |
| Mechanical Keyboard | P200 | YQP-4388 | 20 |
| USB-C Hub | P300 | YQP-6798 | 12 |

The Seller SKU is the product identifier used by the shop, while the SupplierSku identifies the matching item in LegacySupply.

## 6. Stock Synchronization

Inventory changes are handled through the application's existing domain-event flow.

Stock changes can happen because of:

- a normal React order
- a Tiangge order
- a customer cancellation
- a supplier delivery

When inventory changes, the application publishes the updated available quantity to Tiangge instead of relying on a timer to repeatedly send the same stock value.

For a Tiangge order, the application first sends the order decision and then publishes the resulting stock update.

## 7. Tiangge Order Processing

The application polls the Tiangge feed automatically.

Each feed event contains:

- sequence number
- event ID
- event type
- order ID
- order lines
- timestamps

The application stores the feed cursor so it can continue from the correct position after a restart.

It also stores processed event IDs. This is important because Tiangge uses at-least-once delivery and the same event can appear more than once.

### Order flow

```text
Tiangge
   |
   | GET /feed
   v
Channel Module
   |
   | new ORDER_PLACED event
   v
Order Module
   |
   v
Inventory Module
   |
   +---- enough stock ----> ACCEPTED
   |
   +---- no stock and supplier PO ----> BACKORDERED
   |
   +---- cannot fill ----> REJECTED
```

The order is processed using the same Order and Inventory logic used by the rest of the application.

## 8. Duplicate Order Protection

Tiangge can deliver the same event more than once.

The application does not use the feed sequence number alone to identify duplicates. It uses the Tiangge `eventId`.

Processed event IDs are stored in the database. This allows the application to recognize events that were already handled and prevents the same Tiangge order from being processed twice.

The stored event information also survives an application restart.

## 9. Customer Cancellations

Tiangge can send an `ORDER_CANCELLED` event through the same feed.

The application:

1. Finds the corresponding shop order.
2. Uses the existing cancellation logic.
3. Restocks the cancelled items.
4. Confirms the cancellation to Tiangge.
5. Publishes the updated stock.

The cancellation confirmation is sent to:

`POST /orders/{orderId}/cancellation`

## 10. LegacySupply Restocking

The existing LegacySupply adapter from Lab 3 remains inside the Spring Boot application.

When inventory reaches the configured low-stock condition, the application can create a supplier purchase order through the adapter.

The supplier mapping is:

```text
P100 -> YQP-1135
P200 -> YQP-4388
P300 -> YQP-6798
```

Supplier requests also include the current `X-Client-Instance` value.

When a supplier delivery is detected, the inventory is updated. A waiting Tiangge backorder can then be resolved.

## 11. Backorder Flow

A Tiangge order is only marked `BACKORDERED` when the missing product already has an open supplier purchase order.

The general flow is:

```text
Tiangge Order
      |
      v
Not enough stock
      |
      v
Open LegacySupply PO exists?
      |
   +--+--+
   |     |
  Yes    No
   |     |
   v     v
BACK-   REJECTED
ORDERED
   |
   v
Supplier delivery
   |
   v
Inventory updated
   |
   v
Tiangge resolution
   |
   v
ACCEPTED
```

## 12. Environment Variables

Sensitive credentials must not be committed to GitHub.

Set the required values in the environment before starting Spring Boot.

Example PowerShell setup:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="YOUR_DATABASE_USERNAME"
$env:SPRING_DATASOURCE_PASSWORD="YOUR_DATABASE_PASSWORD"

$env:LS_CLIENT_ID="YOUR_STUDENT_ID"
$env:LS_API_KEY="YOUR_LEGACYSUPPLY_API_KEY"
```

The Tiangge integration can use:

```text
TIANGGE_CLIENT_ID
TIANGGE_API_KEY
```

If these are not provided, the application uses the corresponding LegacySupply values:

```text
LS_CLIENT_ID
LS_API_KEY
```

Never place the real API key in Java source code, `.env` files that are committed, README files, or GitHub commits.

## 13. Database Setup

For an existing Lab 2/Lab 3 database, run:

`lab4-migration.sql`

The migration adds the database structures needed for Lab 4 without replacing the existing application data.

The Lab 4 database stores information needed for:

- Tiangge feed cursor
- processed Tiangge event IDs
- Tiangge-to-shop order relationships
- pending stock updates
- backorder processing

For a completely new database, the project's `supabase.sql` can be used as the starting schema.

## 14. Running the Application

Open PowerShell and go to the backend:

```powershell
cd backend
```

Compile the project:

```powershell
mvn clean compile
```

If compilation succeeds, start Spring Boot:

```powershell
mvn spring-boot:run
```

Keep the application running during the marketplace tests.

Once the application starts, it should perform the required Tiangge operations automatically.

## 15. Testing Procedure

### Stage 0 — Postman

Postman can be used to understand the Tiangge API before the application goes live.

The following operations can be tested manually:

1. Heartbeat
2. Listings
3. Stock
4. Feed
5. Order decision
6. Cancellation
7. Backorder resolution

The API key must be supplied through the Authorization header as a Bearer token.

A temporary UUID can be used during Stage 0 testing.

### Stage 1 and later — Application only

Once the application is live, stop sending Tiangge requests manually from Postman.

The grader expects the running Spring Boot application to make the requests.

Check the Tiangge self-check page for:

- application online status
- heartbeat
- listings
- stock updates
- supplier calls
- order decisions

## 16. Restart Test

The restart test checks whether the application can recover without processing old events again.

Procedure:

1. Start the application.
2. Wait until the Tiangge shop is live.
3. Confirm that orders are being received.
4. Stop Spring Boot.
5. Leave it stopped for at least one minute.
6. Allow Tiangge orders to continue arriving.
7. Start Spring Boot again.
8. Check the self-check page.
9. Confirm that missed orders are processed.
10. Confirm that already processed orders are not duplicated.

The saved feed cursor and processed event IDs are used for this test.

## 17. Hands-Off Test

The hands-off test is the final live test.

Before starting it, make sure the earlier checks are passing:

- application is online
- heartbeat is active
- at least 3 listings are published
- listings have supplier mappings
- stock updates are working
- orders are being decided on time
- duplicate orders are handled once
- cancellations work
- supplier calls are coming from the application
- backorders can be resolved
- restart test has passed

When the hands-off test starts, leave the application running and do not use Postman or manually modify the marketplace.

## 18. Reflection

The three reflection questions shown by the Tiangge self-check page are recorded in:

`REFLECTION.md`

The answers should be based on the actual application code, database records, and logs from the completed tests.

## 19. Important Files

| File | Purpose |
|---|---|
| `backend/` | Spring Boot backend |
| `frontend/` | React frontend |
| `supabase.sql` | Main database schema |
| `lab4-migration.sql` | Lab 4 database migration |
| `REFLECTION.md` | Lab 4 reflection answers |
| `README.md` | Project and Lab 4 documentation |

## 20. Lab 4 Checklist

- [ ] Application generates a new UUID on startup
- [ ] First Tiangge heartbeat is sent
- [ ] Heartbeat continues every 30 seconds
- [ ] At least 3 listings are published
- [ ] Each listing has a valid LegacySupply SupplierSku
- [ ] Initial stock is published
- [ ] Stock changes are sent through domain events
- [ ] Tiangge feed is polled automatically
- [ ] Feed cursor is persisted
- [ ] Event IDs are persisted
- [ ] Duplicate events are ignored
- [ ] Tiangge orders use the existing Order and Inventory logic
- [ ] Orders are accepted, rejected, or backordered correctly
- [ ] Customer cancellations are handled
- [ ] Cancelled items are restocked
- [ ] Supplier purchase orders are created by the application
- [ ] Supplier deliveries resolve waiting backorders
- [ ] LegacySupply calls contain the application instance ID
- [ ] Restart test passes
- [ ] Hands-off test passes
- [ ] `REFLECTION.md` is completed
- [ ] Final `lab4-final` tag is created after all tests pass

## 21. Repository Branch

Lab 4 work is developed on:

`lab4-tiangge-marketplace`

The previous branch is kept unchanged so the Lab 4 work can be reviewed separately.
