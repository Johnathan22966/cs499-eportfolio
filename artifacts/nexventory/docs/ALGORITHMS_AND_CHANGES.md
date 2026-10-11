# Milestone Three implementation and tradeoffs

## Rules and data model

`InventoryItem` is immutable. It contains saved ID, name, SKU, nonnegative quantity, and nonnegative minimum stock. Shortage is `max(0, minimumStock - quantity)`. Subtraction is safe for two nonnegative signed 32-bit integers. Invalid or overflowing text is rejected before conversion; model and database checks also reject negative minimums.

An item needs restocking exactly when quantity is below its minimum. Zero minimum disables restocking, including at zero quantity. At the exact minimum an item is Good. Physical stock status is Out for quantity zero, Low for positive quantity below its target, and Good otherwise. Out/Low/Good partition valid inventory; Needs Restocking is a separate filter.

Restock ordering is: eligible records first, eligible zero-quantity records first within that group, descending shortage, case-insensitive name, then ascending stable record ID. With unique database IDs this produces deterministic results even if input order changes. Non-candidates are excluded from the restock view; choosing priority sorting in All Stock places them after candidates in name/ID order.

The planned example produces Adapter (0 available, minimum 4), Patch Cable (3,10), then Spare Switch (2,3). Label Roll (12,5) is excluded. The adapter precedes the cable because being out of stock takes precedence over absolute shortage.

## Collection choice

The production path filters into an ArrayList, then uses Collections.sort with a Comparator. For n retrieved records and k matching records, filtering is O(n), comparison sorting is O(k log k), and additional storage is O(k). Comparison cost also depends on product-name length when the numeric priorities tie. InventoryItem objects are not cloned, and the input list is not reordered.

The comparison implementation offers eligible records to a PriorityQueue and drains it with poll. Repeated insertion and removal are O(k log k) overall, in addition to O(n) filtering, and use O(k) storage. Heap iteration is not sorted. A heap can suit repeated next-item retrieval, but the current feature displays the whole ordered list, making the sorted ArrayList simpler. Java's standard sort and heap are used; this is not a claim to have authored a new sorting algorithm.

Search remains parameterized SQLite filtering from Milestone Two: partial SKU/product name, or exact numeric item ID. Percent, underscore, and backslash are escaped as literals. SQLite LIKE's ordinary case-insensitive matching is ASCII-based; full Unicode case folding is not promised. New stock filtering/sorting runs after search. The enhanced query no longer pre-sorts in SQL, avoiding an unnecessary second sort. Summary counts cover all saved inventory, while the displayed result count covers the current search/filter.

A hash map would help repeated exact-key lookup but would not directly solve partial-name matching or ranking. No hash map was added solely to increase the number of structures used.

## Persistence and interface

Database version 3 adds minimum_stock with NOT NULL, DEFAULT 5, and CHECK(minimum_stock >= 0). Version-two inventory and account values are preserved. Existing and sample items start with target 5; users can edit it or use 0. The earlier version-one-to-two password conversion remains in the upgrade chain. Desktop checks cover version-two-to-three schema behavior; The subsequent user-run connected Android test task succeeded; see VERIFICATION.md for the available evidence and reporting limits.

The minimum field is loaded when editing, saved on add/update, and reset to 5 for a new item. Cards show minimum and shortage. The Show Restock List button clears the search and selects Needs Restocking plus Restock Priority. Stock filters and sort menus compose with the existing search tabs. Empty results have an explicit message. Home/dashboard Low Stock counts use per-item targets, while Out of Stock counts remain physical zero-quantity counts. Optional SMS decisions now require a shortage; existing opt-in and Android permission checks remain in place. No SMS was sent during verification.

## Scope and limitations

The milestone follows the code-review priority rules and adds algorithm evaluation. Category and location search require new fields and remain deferred with the broader database work. Room, CSV import, and barcode lookup are not implemented here. Ranking does not model equipment criticality, cost, lead time, or purchasing quantities.

Benchmark timings cover only in-memory selection/ranking. They exclude database queries, view creation, disk access, and Android runtime behavior. The existing ScrollView creates one card per displayed item and database work runs on the screen thread. For large real inventories, pagination/RecyclerView and background queries should be evaluated before claiming smooth phone performance. PriorityQueue also has no advantage demonstrated here for displaying an entire large result.

## Revision history

September 27, 2026 — Preserved the original CS 360 source and unchanged Milestone Two starting version. Added the minimum-stock field, safe input handling, schema upgrade, priority comparator, ArrayList selection, heap comparison, sorting/filter controls, display/count/SMS consistency, tests, benchmark and technical evidence. Bumped Android versionCode to 2 and versionName to 1.1. SQLite schema version is 3. Subsequent Android Studio unit and connected instrumentation tasks succeeded, and the user demonstrated the manual checks recorded in VERIFICATION.md.
