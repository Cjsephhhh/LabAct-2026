# Lab 4 Reflection

## 1. Tiangge order TG-7M8P7J (4 × P300) was accepted at 18:05:21. At that moment your last published stock for P300 was 0, and the stock Tiangge worked out from your own decisions, cancellations and deliveries was 0. Where did your application’s stock figure come from, and why did it disagree?

The P300 stock shown by the marketplace was the last stock value that the application had published before the order. The actual stock used by the application came from its own Inventory records and the stock changes caused by orders, cancellations, and supplier deliveries. Because those changes can happen after a stock publication, Tiangge can temporarily show an older value such as 0 while the application's current inventory has already changed. The event-driven stock synchronization is meant to publish the new value after each committed inventory change.

## 2. Event evt_de7a0dbfdc96012 (order TG-7M8P7J) reached your application twice, as seq 1 and seq 2, and you processed it once. Show the code and the stored data that made the second delivery harmless, and explain what would happen if your application restarted between the two.

The application checks the Tiangge eventId before processing an event in TianggeFeedPoller. After the first successful processing, the eventId and sequence number are stored in the tiangge_processed_events table, so the second delivery with the same eventId is skipped. The feed cursor is also stored in tiangge_state, so the progress is kept in Supabase instead of memory. If the application restarted between the two deliveries, the stored eventId would still be found after startup and the duplicate event would still not create another shop order.

## 3. Order TG-DUWC8F was backordered at 05:18:06 and accepted at 05:20:44, after PO-103031 was delivered at 05:20:32. Trace how the delivery reached your Inventory and what then resumed the backorder order.

The backorder was created when the application found that there was not enough P300 stock and created the required supplier reorder. SupplierOrderScheduler later polled the open supplier order and detected the delivery of PO-103031, then published SupplierOrderDeliveredEvent. The supplier delivery listener restocked Inventory, and TianggeBackorderListener then retried the waiting backorder; once the order had enough stock, it was accepted and Tiangge received the resolution. This connects the supplier delivery, Inventory update, existing OrderService logic, and Tiangge backorder resolution without manually using Postman.
