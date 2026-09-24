# LegacySupply Integration

## Product Mapping

| Product ID | Product Name | SupplierSku | PackSize |
|---|---|---|---:|
| P100 | Wireless Mouse | YQP-1135 | 24 |
| P200 | Mechanical Keyboard | YQP-4388 | 20 |
| P300 | USB-C Hub | YQP-6798 | 12 |

The SupplierSku and PackSize values were obtained from the
LegacySupply catalog endpoint using an authenticated session.

## Purchase Order Test

A test purchase order was successfully created through LegacySupply.

| Field | Observed Value |
|---|---|
| SupplierSku | YQP-1135 |
| Qty | 1 |
| Uom | CS |
| BuyerRef | LAB3-TEST-001 |
| PO Number | PO-100054 |
| StatusCode | 10 |
| HTTP Status | 201 Created |

The Wireless Mouse has a PackSize of 24. Therefore, Qty 1 with
Uom CS represents one case containing 24 individual units.

## Purchase Order Tracking

The purchase order PO-100054 was initially accepted with StatusCode 10.
A later tracking request returned StatusCode 40, meaning the order was
delivered.

| StatusCode | LegacySupply Meaning | Our Meaning |
|---|---|---|
| 10 | Accepted | ACCEPTED |
| 20 | Picking | PICKING |
| 30 | Shipped | SHIPPED |
| 40 | Delivered | DELIVERED |

## BuyerRef Lookup

LegacySupply supports looking up purchase orders using BuyerRef.

A lookup using LAB3-TEST-001 returned one purchase order:

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

## Observed Errors

| Error Code | Cause |
|---|---|
| E-AUTH-07 | The LegacySupply session was no longer valid. |
| E-SYS-99 | LegacySupply was temporarily unavailable while requesting a session. |

## Session

LegacySupply sessions are short-lived. The exact session lifetime
will be measured during the integration testing phase.

## Qty and Uom

Qty represents the number of supplier-defined units being ordered,
while Uom identifies the unit of measure.

For example, the Wireless Mouse has a PackSize of 24. A request with
Qty 1 and Uom CS represents one case containing 24 individual units.
Therefore, if the application needs 25 individual units, the adapter
must round the requirement up to 2 cases, resulting in 48 units.