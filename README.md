# IT342 System Integration — Lab 4: Tiangge Marketplace

**Project:** LabAct-2026  
**Branch:** `lab4-tiangge-marketplace`  
**Module:** Tiangge Channel Integration  
**Stack:** Spring Boot + React + Supabase PostgreSQL

## Lab 4 Overview

This lab extends the existing modular monolith so the Spring Boot application can run the shop **unattended** through the Tiangge Marketplace.

The Spring Boot application is the only component that communicates with the external Tiangge Marketplace and LegacySupply services. Tiangge orders must go through the same existing Order and Inventory business logic used by the React application.

### Main Lab 4 Goals

1. Create a fresh client-instance UUID every time the application starts.
2. Send the client instance ID with outbound Tiangge and LegacySupply requests.
3. Send a Tiangge heartbeat every 30 seconds.
4. Publish at least three marketplace listings using the existing LegacySupply SupplierSku mappings.
5. Synchronize stock through domain events instead of a stock-sync timer.
6. Poll the Tiangge order feed, remember the cursor, deduplicate events, and create real shop orders.
7. Handle Tiangge cancellations using the existing cancellation and inventory-restock logic.
8. Automatically reorder missing stock from LegacySupply.
9. Persist the feed cursor and processed event IDs so a restart does not duplicate work.
10. Keep the application running unattended for the required 10-minute test.

## Current Lab 4 Progress

### Task 1 — Client Instance

Implemented on this branch:

- `ClientInstance` generates a new UUID when Spring starts.
- LegacySupply requests automatically include:
  `X-Client-Instance: <startup UUID>`
- The client ID is not regenerated for every request.
- The implementation is isolated from the Order and Inventory modules.

**Current commit:** `e4cd095ab242b113d59656ba51be102e77253ba1`

> The Tiangge endpoint paths and request/response formats will be implemented from the official Tiangge lab manual rather than guessed.

## Product / Supplier Mapping

| Shop Product | LegacySupply SupplierSku | Pack Size |
|---|---|---:|
| P100 Wireless Mouse | YQP-1135 | 24 |
| P200 Mechanical Keyboard | YQP-4388 | 20 |
| P300 USB-C Hub | YQP-6798 | 12 |

These mappings are used when the application creates supplier purchase orders and resolves stock backorders.

## Architecture

The application keeps the existing modular-monolith structure:

- `edu.cit.alvarado.shop` — Order module
- `edu.cit.alvarado.inventory` — Inventory module
- `edu.cit.alvarado.supplier` — LegacySupply adapter
- `edu.cit.alvarado.channel` — Tiangge/channel integration

The channel module should expose only its public integration contract and domain types. HTTP clients, feed polling, JSON/transport classes, and translators should remain package-private.

The Order and Inventory modules must **not depend on Tiangge**. Tiangge orders enter through the channel module and are then passed into the existing Order/Inventory business rules.

## Environment Variables

Never commit real credentials or API keys.

Existing database configuration:

```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME="postgres.YOUR_PROJECT_REF"
$env:SUPABASE_DB_PASSWORD="YOUR_DATABASE_PASSWORD"
```

LegacySupply:

```powershell
$env:LEGACY_SUPPLY_BASE_URL="https://legacysupply.onrender.com/api/v1"
```

Tiangge API credentials/configuration should also be supplied through environment variables. The actual variable names must match the Tiangge lab manual.

## Running the Project

Backend:

```powershell
cd backend
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

For the unattended Lab 4 test, the Spring Boot backend must stay running continuously.

## Lab 4 Testing Checklist

Before final submission, verify:

- [ ] Application generates a new client UUID after every restart.
- [ ] Tiangge requests contain the same UUID during one run.
- [ ] LegacySupply requests contain the same UUID during one run.
- [ ] Heartbeat is sent every 30 seconds.
- [ ] Three Tiangge listings are published with the correct SupplierSku values.
- [ ] A stock change reaches Tiangge through an event-driven flow.
- [ ] Tiangge orders become real Order-module orders.
- [ ] Accepted, rejected, and backordered cases are handled.
- [ ] Multi-item orders are all-or-nothing.
- [ ] Duplicate feed events do not create duplicate orders.
- [ ] Feed cursor survives application restart.
- [ ] Processed event IDs survive application restart.
- [ ] Tiangge cancellation restocks inventory.
- [ ] Cancellation confirmation is sent back to Tiangge.
- [ ] LegacySupply reorder is created when stock is needed.
- [ ] Backorders resolve after supplier delivery.
- [ ] The application completes the required 10-minute hands-off test.

## Evidence

Keep screenshots/logs showing:

1. Client instance UUID and outbound headers.
2. Tiangge heartbeat.
3. Published listings.
4. Stock synchronization.
5. Accepted/rejected/backordered Tiangge orders.
6. Cancellation and restock.
7. LegacySupply reorder and delivery.
8. Restart with the same feed cursor and no duplicate processing.
9. The final 10-minute unattended run.

Put final reflection answers in `REFLECTION.md` as required by the lab instructions.

## Previous Modular Monolith Features

The project still contains the original modular-monolith functionality:

- Multi-item orders with all-or-nothing transactional reservation.
- Order cancellation with inventory restock.
- Inventory and order REST endpoints.
- Spring application events.
- Low-stock notifications.
- Package-private Inventory implementation.
- Supabase PostgreSQL persistence.

## Important Rule

**Do not put Tiangge API calls inside React, OrderService, or InventoryService.**

All external Tiangge and LegacySupply communication belongs in the backend integration layer so the application can continue processing marketplace activity even when nobody has the frontend open.
