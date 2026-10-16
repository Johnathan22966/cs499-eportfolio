# Verification results — October 3, 2026

## Observed results

- Standalone CSV checks: 427 assertions passed.
- Inherited algorithm regression checks: 59,321 assertions passed (includes loops and randomized inputs).
- Gradle JUnit task: 13 tests passed, zero failures, errors, or skips. Three are RoomDatabaseJvmTest cases using Robolectric's simulated Android API 28 with its legacy SQLite backend. Their upgrade test exercises versions 1, 2, and 3.
- Main application debug APK and Android instrumentation test APK built successfully.
- All 15 application XML files parsed successfully and referenced controls were checked.
- Original source preserved byte for byte; Milestone Three baseline preserved byte for byte, excluding only local IDE/build files.

The actual Room tests verify retained item/account IDs, highest assigned ID, login hashes, default/restored stock targets, category/location persistence after reopening, CRUD, rejected duplicates/invalid batches, database stock rules, and full rollback after an injected second-row write failure. Test databases are temporary and isolated.

## Scope and limitations

Later emulator verification on October 3, 2026 completed successfully: the full instrumentation suite reported OK (6 tests), including three Room database tests, two restocking database tests, and one application-context test. The raw result is in Emulator_Test_Results.txt. Hands-on checks confirmed saved category and location edits, a two-record CSV import, rejection of existing and repeated item codes and negative quantities, persistence after a full app restart, searches, stock filters, and both import cancellation paths. See Emulator_Validation_Addendum.md for the detailed record.

Physical-device behavior and SMS delivery were not verified. Sample-data controls, manual logout and password checks, every invalid form case, blank optional fields, deletion, and full multi-item sort order comparisons were not repeated in this session. The entire manual checklist is therefore not marked complete.

The Windows host could compile source but failed when javac closed its jar file systems because the sandbox denied a Windows path operation. A temporary compiler-API harness performed the JavaCompile tasks without explicitly closing that file manager; the process then released its handles. Gradle completed tests, packaging, and APK assembly. A temporary official Android SDK was used. The harness, temporary SDK, cache, machine-specific local.properties, and development signing key are excluded from this package. Open enhanced in a normal Android Studio setup to reproduce standard builds.

## Repeat checks

Open enhanced as the Android Studio project, configure installed SDK 36.1/build-tools 36.0.0 and the compatible bundled JDK, and sync Gradle. From its terminal run:

    .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest

With a connected test emulator (not a device holding the user's live inventory):

    .\gradlew.bat :app:connectedDebugAndroidTest

Plain Java checks:

    .\verification\run-checks.ps1 -JdkBin 'PATH_TO_JDK\bin'

The historical check_sql.py now points to the actual Room suite; it is no longer an independent database test. Detailed XML and the final Gradle log are included beside this report. Earlier milestone reports are historical, under milestone_three_history.
