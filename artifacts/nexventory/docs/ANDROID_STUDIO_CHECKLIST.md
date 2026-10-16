# Android Studio finishing checklist

Use a test emulator or a disposable test installation. Keep the original coursework and any live inventory separate.

1. Open the enhanced folder in Android Studio. Configure the SDK and sync. Run the build and automated test commands in Verification_Results.md.
2. Launch the application. Register a test account, close/reopen, and log in. Confirm the home counts and inventory navigation work.
3. Add an item with category and location. Edit it, close/reopen, and confirm those values persist. Search by name and SKU; test restock sorting/filtering and minimum stock editing.
4. Use Import Inventory CSV and choose samples/valid_inventory.csv. Confirm the import and check that exactly two new items appear with the right stock/category/location.
5. Import that same file again: expect duplicate rejection and no change to the count. Try samples/invalid_negative.csv and samples/duplicate_in_file.csv: expect clear errors and zero inserted rows. Cancel the picker and confirmation dialog: expect no changes.
6. Run connectedDebugAndroidTest. Its isolated databases check migration, preserved records/accounts, restock behavior, and all-or-nothing imports. Record the actual result and any issue.
7. Review the journal table before uploading: confirm the algorithms submission and each portfolio upload status. Current wording reflects the last available records, not an assumed upload.
8. Submit the database source ZIP and narrative through the course's Milestone Four assignment, and the journal through Module Five. Incorporate feedback into the final portfolio and self-assessment.

No assignment was submitted or portfolio published by this session.
