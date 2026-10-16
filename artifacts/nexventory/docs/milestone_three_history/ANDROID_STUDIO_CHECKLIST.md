# Android Studio checks before submission

Status: the user completed the demonstrated manual checks and supplied successful Android unit and connected instrumentation task results. See VERIFICATION.md for the precise evidence and any cases not individually demonstrated. This checklist is retained as a repeatable procedure, not a claim that every optional step was run.

## Open and build

1. Extract the ZIP to a new folder. In Android Studio, choose Open and select `enhanced` (the folder containing settings.gradle). Keep your previous project and emulator data available for comparison.
2. Let Gradle sync. Use the JDK 21 toolchain and the project's configured Android 36.1 SDK. Build the project. If anything fails, retain the first error message for diagnosis rather than changing dependency versions at random.
3. Run the tests in `app/src/test`. With an emulator connected, run `RestockDatabaseTest` in `app/src/androidTest`. Its database tests use a uniquely named cache database rather than the real inventory database. Record build and test outcomes below.

## Existing inventory and migration

4. For the preservation check, update the existing Milestone Two app on the same emulator, using the same signing key and application ID. Do not uninstall or clear app data, since that would erase the data being tested. If Android reports a signature mismatch, stop and report it rather than uninstalling to get past the warning.
5. Log in with your existing account. Confirm the four pre-CS-499 records still exist with their original names, SKUs and quantities. They should now show minimum 5. At quantity exactly 5 the new status is Good, a deliberate change from the old inclusive threshold.
6. Keep SMS opt-out off during these tests so no message is sent. Verify it remains off after logout and login.

## Restocking example

Use unique `M3-TEST-` SKUs for the following temporary items. The normal sample-removal button does not remove them.

| Name | SKU | Quantity | Minimum | Expected shortage |
|---|---|---:|---:|---:|
| Adapter | M3-TEST-A | 0 | 4 | 4 |
| Patch Cable | M3-TEST-C | 3 | 10 | 7 |
| Spare Switch | M3-TEST-S | 2 | 3 | 1 |
| Label Roll | M3-TEST-L | 12 | 5 | 0 |
| No Target | M3-TEST-N | 0 | 0 | 0 |

7. Tap Show Restock List. It clears any search. Choose SKU search and enter `M3-TEST-` to isolate the demonstration. The displayed order must be Adapter, Patch Cable, Spare Switch. Label Roll and No Target must be excluded.
8. Select Patch Cable and update its quantity to 10. It should disappear from Needs Restocking. Find it using All Stock; its status should be Good and shortage zero.
9. Edit Spare Switch's minimum to 0. Confirm it no longer needs restocking. Close and reopen the app; find the item under All Stock and confirm its minimum remains 0.
10. Select Out of Stock. Adapter and No Target should appear within the test SKU search, even though No Target does not qualify for restocking. Check Low Stock, Good Stock, and All Stock, then sort by name and quantity.
11. Give two temporary items equal shortages and quantities above zero. Confirm name ordering. For identical names (with distinct SKUs), confirm the smaller item number appears first.
12. Search by name, SKU, and item number while a filter is active. A nonexistent search should show No matching items. Clear the search and choose All Stock to return to everything. Compare global summary labels with the current result-count label.

## Invalid values and final checks

13. Try a blank minimum, a very long number such as 9999999999999999999, and pasted -1 or 1.5 if the keyboard allows them. Saving should show a minimum-field error and leave the previous saved record unchanged. Also check minimum 0 and quantity exactly equal to minimum.
14. Check that item cards and the new menus fit on the phone screen. Try selecting an item, editing it, cancelling deletion, logout/login, and returning from the notification settings.
15. Delete only the temporary M3-TEST records individually after testing, if desired. Preserve the original inventory.

## Results to report

- Build and Gradle sync:
- Unit tests and RestockDatabaseTest:
- Existing login and four records preserved:
- Planned order correct:
- Target changes persisted after reopening:
- Invalid values rejected without changing saved data:
- Filters/search, empty results, phone layout, and SMS opt-out:
- Any error message or unexpected behavior:

Review the Word narrative to make sure its reflection represents your experience. The verification notes and narrative have been updated with the supplied Android results. Review the narrative for personal accuracy before submitting the ZIP and DOCX.
