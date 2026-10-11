# Historical Milestone Two software design and engineering notes

These notes describe the inherited Milestone Two work. This project now also includes the Milestone Three restock algorithm, minimum-stock targets, sorting, filters, and verification harness. See the submission's `docs/ALGORITHMS_AND_CHANGES.md` and `docs/VERIFICATION.md` for the current enhancement and its verification status. The later-enhancement scope at the end of this historical note reflects the earlier milestone, not the current feature set.

Original artifact: CS 360 Nexventory Android application. This enhanced copy is for CS 499 Milestone Two.

## Completed changes

- Added `InputRules` to reject negative, fractional, missing, and overflowing quantities before conversion can crash the activity.
- Connected the search field to a parameterized name and SKU query. Percent and underscore are escaped so they search literally.
- Replaced new plain text passwords with salted PBKDF2 hashes. Database version 2 upgrades existing user rows without dropping the inventory or user tables.
- Added database validation for names, SKUs, and nonnegative quantities on create and update. New installations also have a nonnegative quantity constraint.
- Added unit tests for quantity parsing and corrected the search hint to match implemented behavior.
- Added `InventoryRepository` for inventory access from screens and `SmsAlertService` for alert decisions.
- Saved an explicit SMS opt-in choice, checked it before attempting an alert, and stopped claiming delivery without confirmation.
- Added delete confirmation and distinguished duplicate SKU failures from other save failures.
- Stopped trimming passwords, which could silently change the credential a user entered.
- Moved Add and Update buttons below the item fields, renamed the inventory heading, and scrolls the form into view when Edit is tapped.
- Added a dashboard Log Out button and session checks for the inventory and settings screens. Logging out clears the activity stack so Back cannot reopen inventory.
- Made inventory rows selectable for editing, displayed each SKU in the row, added no-results feedback, and replaced the fixed example alert with live stock counts.
- Split search into SKU, Item Number (the saved database record ID), and Product Name tabs. Item numbers are shown in rows.
- Added an optional, confirmed loader for 15 generic networking sample items with `SAMPLE-NET` SKUs. Repeated loads skip existing sample SKUs.
- Added a confirmed Remove Sample Items action that deletes only the 15 listed sample SKUs. Added persistent field labels and an editing-item number above the populated form; Update is disabled until a row is selected.
- Replaced the wide inventory table with phone-width item cards so product name, item number, SKU, quantity, and status remain visible without horizontal scrolling. Tapping a card opens the edit form. A single Delete Selected Item button now sits with the update action instead of repeating in every card.
- Clear stale field errors when selecting a different item or resetting the entry form.

## Verification

- `InputRules.java` compiled with JDK 21.
- All 15 Android XML files parsed successfully.
- The user reported that the Android Studio project builds, the 15 samples load, all three search modes return results, SMS alerts remain off after logout and login, a Cat6 Keystone Jack edit from 5 to 500 appears in the inventory list, logout works, and Remove Sample Items works. All four inventory records created before CS 499 remained after sample removal. An oversized quantity showed "Quantity is too large" while the app remained open; selecting the laptop again showed its prior saved quantity of 5. The user also confirmed that the new selected-item Delete button works, Cancel preserves the item, and the revised item layout looks better. Those four records are baseline data, not Milestone Two additions. The local build process could not read the installed Android 36.1 SDK platform files.

## Manual checks in Android Studio

1. Create an account, close the app, then log in with the same credentials. Try an incorrect password.
2. Add an item with quantity 0, then edit it to quantity 5. Confirm the home and inventory counts agree.
3. Enter an extremely large quantity. Confirm the field shows an error and the app stays open.
4. Search by part of a name and SKU; clear the field to show all items.
5. Verify a duplicate SKU is rejected and deleting a row refreshes the totals.
6. Cancel a deletion and confirm the item remains. Disable SMS alerts, navigate away and back, then confirm the setting remains off.
7. Log out from the dashboard. Confirm Back does not reopen inventory, incorrect credentials stay on the login screen, and correct credentials restore access to saved items.
8. Tap an item row. Confirm it highlights and scrolls to a populated edit form. Change the quantity, tap Update Selected Item, and confirm the table and summaries refresh. Search for a nonexistent SKU and confirm the no-results message appears.
9. Tap Load 15 Sample Items and confirm 15 appear; tap it again and confirm no duplicates are added. Search for one sample by SKU, by its displayed Item Number, and by part of its Product Name.
10. Cancel Remove Sample Items and confirm the list is unchanged. Then remove them and confirm only the listed sample SKUs disappear and other manually entered records remain.
11. On a narrow emulator screen, confirm every item card shows quantity and status without sideways scrolling. Select one and confirm Delete Selected Item becomes available in the edit form; Cancel must preserve the record.

## Scope for later enhancements

Per-item restock ranking, Room migration, CSV import, and barcode lookup belong to the later algorithms and databases categories. This milestone implements the software design and engineering portion of the Module One plan.
