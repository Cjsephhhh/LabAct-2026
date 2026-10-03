# Lab 4 Reflection

## 1. Tiangge may deliver the same event more than once. Describe how your application recognises an event it has already handled, where that knowledge is stored, and whether it survives a restart.

The application uses the `eventId` from each Tiangge feed event as its unique identifier. Before handling an event, `TianggeFeedPoller` checks the `tiangge_processed_events` table and skips the event when its ID is already stored. The event ID and sequence number are saved after the event is handled, and the feed cursor is saved in `tiangge_state`. Because both are stored in Supabase, the information is still available after the application is restarted.

## 2. Pick one order your application accepted. Trace it from the feed to your database to the stock update you sent Tiangge, naming each class it passes through.

An accepted order starts in the Tiangge feed and is read by `TianggeFeedPoller`. The feed lines are converted into `OrderRequest`, then `OrderService` validates and reserves the items through `InventoryService`. The order and its items are saved in the Order module, while the inventory reservation publishes the existing `StockChangedEvent`. `TianggeStockListener` receives that event after the transaction commits and sends the new stock quantity to Tiangge.

## 3. Your application now talks to two external systems with very different interfaces. Which of your modules know about Tiangge, which know about LegacySupply, and what would change if Tiangge were replaced by another marketplace?

Only the `channel` module knows the Tiangge API details such as the heartbeat, feed, decisions, cancellations, and stock endpoints. The `supplier` module contains the LegacySupply adapter, while Order and Inventory only use their own services and domain events. If Tiangge were replaced, the marketplace client and feed handling could be changed inside the channel module without changing the Order or Inventory modules. The public marketplace interface also keeps the external API details separate from the rest of the application.
