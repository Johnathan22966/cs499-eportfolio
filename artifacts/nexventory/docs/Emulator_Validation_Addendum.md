# Nexventory validation results — October 3, 2026

Prepared for Johnathan Roehsner. The two existing Word documents were preserved.

## Passed on the running Android emulator

- Created a test inventory item with a quantity, minimum stock, category, and location.
- Edited the item; the saved values returned correctly when the inventory screen reopened.
- Confirmed the low-stock warning changed correctly after increasing the quantity.
- Rejected a duplicate SKU entered manually.
- Imported valid_inventory.csv: exactly two items were saved, including the quoted name "Cable, blue".
- Rejected the same CSV on a second import because its SKUs already existed.
- Rejected invalid_negative.csv without adding items.
- Rejected duplicate_in_file.csv without adding either row.
- Ran the full instrumentation suite on the emulator: **OK (6 tests)**. These include actual Room database upgrades from versions 1–3, account and item preservation, ID continuity, saved edits, reopening, and transaction rollback. Test databases were disposable.
- Read the app's saved inventory after the tests and confirmed exactly three items remained:

| ID | Item | SKU | Quantity | Minimum | Category | Location |
|---|---|---|---|---|---|---|
| 1 | Validation cable | CHECK-CABLE-01 | 7 | 5 | Network supplies | Shelf B |
| 2 | Cable, blue | M4-DEMO-CABLE | 2 | 5 | Cables | Shelf A |
| 3 | Spare adapter | M4-DEMO-ADAPTER | 6 | 3 | Adapters | Drawer 2 |

## Evidence

Screenshots and the complete emulator test result are in Validation_Evidence beside this report. Earlier build and local test evidence remains in the source package.

## Later checks completed

- Full app force-stop and restart retained the signed-in session and correct counts.
- Searches by SKU and product name returned the expected item.
- All five stock-filter choices returned the expected counts (3 all, 1 needs restocking, 0 out, 1 low, 2 good).
- Quantity and restock sorting controls selected successfully; the restock shortcut displayed the low-stock item. Full multi-item order comparisons were not repeated in this session.
- Both picker cancellation and confirmation cancellation returned to the app without adding rows. A database read afterward confirmed the same three records and values.
- Updated copies of both Word documents were rendered and visually checked, and their font, spacing, margins, name, and table structure passed checks.

## Remaining checks

The entire 17-step checklist has not been completed. Sample-data controls, manual logout and login with correct and incorrect passwords, all invalid form-entry cases, optional blank fields, deletion, and full multi-item sorting comparisons were not repeated in this session. Automated database tests and earlier milestone evidence cover several of these behaviors. No submission or ePortfolio upload was performed. The original Word documents retain their earlier wording. Use the copies with _Updated in their filenames for the current results. The user confirmed that no portfolio website exists. The algorithms milestone submission was confirmed by the supplied submission screenshot. GitHub upload versions remain unverified.

The three listed items are newly created validation data. This session does not establish what items were present in an earlier installation.
