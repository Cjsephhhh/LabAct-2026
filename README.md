<img width="1800" height="931" alt="image" src="https://github.com/user-attachments/assets/641e90fc-c915-4edb-bda5-1c6adf2e9d4d" />


# System Integration - Modular Monolith

New activity project: Spring Boot + React + Supabase.

## Package naming

- `edu.cit.alvarado` - parent package
- `edu.cit.alvarado.shop` - Order module
- `edu.cit.alvarado.inventory` - Inventory module

## Architecture

React communicates with Spring Boot through HTTP/REST.

Inside Spring Boot, Order calls Inventory through the `InventoryService` interface in-process. There is no network call between Order and Inventory.

Spring Boot connects to the shared Supabase PostgreSQL database.

## Required products

- P100 Wireless Mouse - 25
- P200 Mechanical Keyboard - 10
- P300 USB-C Hub - 0

## Run backend

Set these environment variables before running:

- `SUPABASE_DB_URL`
- `SUPABASE_DB_USERNAME`
- `SUPABASE_DB_PASSWORD`

Then:

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

## Run frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

## API

POST `/api/orders`

Example request:

```json
{
  "productId": "P100",
  "quantity": 2
}
```

Expected confirmed response contains `status: CONFIRMED`.

For a rejected test, use P300 with quantity 1 because its stock starts at 0.

## Submission evidence

Add screenshots to `docs/`:

- `confirmed-network.png`
- `rejected-network.png`

The screenshots should show the browser Network tab, request payload, and response.

## Reflection

### 1. In-process integration vs. microservices

In this modular monolith, Order and Inventory are separate modules inside one Spring Boot application. Order communicates with Inventory through the InventoryService interface using a normal Java method call. Because both modules run in the same application, there is no network connection, HTTP request, serialization, service discovery, or distributed authentication needed between them. Error handling is also simpler because method calls return directly.

If Order and Inventory were separate microservices, communication would happen over a network, such as REST. We would need to add an HTTP client, API endpoints, service URLs or discovery, timeouts, retries, authentication, and handling for network failures. Distributed data consistency would also become an important concern. The monolith therefore gives us many local integration features for free, while microservices provide stronger deployment and scaling independence at the cost of additional infrastructure.

### 2. Why package-private InventoryServiceImpl matters

The InventoryService interface is the public contract of the Inventory module. InventoryServiceImpl is package-private so code in the Order module cannot directly depend on the implementation class. Order only receives the InventoryService interface through constructor injection.

If InventoryServiceImpl were public, the Order module could import and depend directly on the implementation. That would weaken the module boundary because Order would become coupled to Inventory's internal implementation instead of depending only on the stable interface. Keeping the implementation package-private encourages the intended architecture and makes the internal details of Inventory easier to change later.

### 3. Extracting Inventory into a microservice

Inventory should be extracted when it needs independent deployment, scaling, ownership, or when multiple applications need to use it. The code would change from an in-process interface call to a network API. The Inventory module would become its own Spring Boot service with REST endpoints. Order would use an HTTP client instead of directly calling InventoryServiceImpl. We would also need service configuration, authentication, timeout and retry handling, and error handling for unavailable Inventory services. Database ownership would need to be reconsidered as well, because independent microservices normally avoid sharing the same database tables directly.
