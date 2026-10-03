# IT342 System Integration — Lab 4: Tiangge Marketplace

Spring Boot + React + Supabase PostgreSQL modular monolith with Tiangge and LegacySupply integration.

## Modules

- `edu.cit.alvarado.shop` — Order module
- `edu.cit.alvarado.inventory` — Inventory module
- `edu.cit.alvarado.notification` — domain events and notifications
- `edu.cit.alvarado.supplier` — LegacySupply integration
- `edu.cit.alvarado.channel` — Tiangge marketplace integration

The Order and Inventory modules do not know that Tiangge exists. Tiangge orders enter through the channel module and use the same Order and Inventory services as orders from the React application.

## Lab 4 features

- New UUID client instance on every Spring Boot startup.
- Tiangge heartbeat every 30 seconds.
- Three marketplace listings mapped to LegacySupply SupplierSku values.
- Stock updates sent from the existing inventory domain event flow.
- Tiangge feed polling every few seconds.
- Durable feed cursor and processed event IDs.
- ACCEPTED, REJECTED, and BACKORDERED decisions.
- Duplicate event protection using Tiangge `eventId`.
- Customer cancellation, restocking, and cancellation confirmation.
- LegacySupply reorder through the existing adapter.
- Backorder resolution after supplier delivery.
- `X-Client-Instance` on Tiangge and LegacySupply calls.

## Product mapping

| Product | Seller SKU | LegacySupply SupplierSku | Pack |
|---|---|---|---:|
| Wireless Mouse | P100 | YQP-1135 | 24 |
| Mechanical Keyboard | P200 | YQP-4388 | 20 |
| USB-C Hub | P300 | YQP-6798 | 12 |

## Environment variables

Do not commit the API key or database password.

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="postgres.YOUR_PROJECT_REF"
$env:SPRING_DATASOURCE_PASSWORD="YOUR_DATABASE_PASSWORD"
$env:LS_CLIENT_ID="YOUR_STUDENT_ID"
$env:LS_API_KEY="YOUR_LEGACYSUPPLY_API_KEY"
```

The Tiangge client uses `TIANGGE_CLIENT_ID` and `TIANGGE_API_KEY` if they are set. Otherwise it uses `LS_CLIENT_ID` and `LS_API_KEY`.

## Database

If your Supabase database already contains the Lab 3/Lab 2 tables and data, run `lab4-migration.sql`. It adds the `BACKORDERED` status and the Tiangge cursor, processed-event, pending-stock, and order-link tables without resetting your existing orders.

For a completely fresh database, `supabase.sql` contains the full Lab 4 schema and seed inventory.

## Run

```powershell
cd backend
mvn clean compile
mvn spring-boot:run
```

Keep the backend running. The application sends its first heartbeat, publishes the listings and stock, and then starts polling the Tiangge feed.

## Testing

Stage 0 Postman testing is allowed before the application goes live. After the running application has sent its heartbeat and listings, stop using Postman for Tiangge actions because the live grader expects external calls to come from the Spring Boot application.

Check the Tiangge self-check page after starting the backend. The UUID printed in the application log should match the instance ID seen by Tiangge.

For the restart test, stop the backend for at least one minute while orders continue arriving. Start it again and check that the saved cursor and processed event IDs prevent duplicate order processing.

## Final evidence

Capture the self-check results for the heartbeat, listings, stock updates, order decisions, duplicate handling, cancellations, backorder delivery, supplier calls, restart test, and hands-off test. Put the three marketplace reflection questions and answers in `REFLECTION.md`.
