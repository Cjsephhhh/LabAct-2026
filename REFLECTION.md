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
