# Verification evidence

Date: September 27, 2026. These are observed results, with the execution boundaries below.

## Passed desktop checks

1. `verification/AlgorithmChecks.java`: **59,321 assertions passed**. Includes the code-review example; empty input and no candidates; exact/zero targets; integer limits; invalid minimum input; stock filters; name/quantity/priority sorting; source-list preservation; deterministic results after shuffling; comparator symmetry/transitivity checks; and sorted-list/heap equivalence across 100 seeded randomized inventories. The count includes assertions inside loops, not 59,321 distinct test methods.
2. Standalone JUnit 4.13.2: **6 tests passed**, consisting of three new InventoryAlgorithmsTest methods, two existing InputRulesTest methods, and one inherited arithmetic example. Compiled using local JDK 8 and run under Java 21; Initial agent-side Gradle execution was blocked; subsequent user-run Android Gradle tasks succeeded as recorded below. The meaningful new suite is supplemented by the larger dependency-free harness.
3. `verification/check_sql.py`: passed desktop SQLite 3.53.1 checks using CREATE/ALTER SQL extracted from DatabaseHelper. Tested version-two inventory and account-value preservation, new-install schema, nonnegative/NOT NULL minimum constraints, counts, and target edits. This is a SQL simulation, not execution of Android SQLiteOpenHelper or its password-migration code.
4. All 15 Android XML resource/manifest files parsed successfully. This establishes XML well-formedness, not Android resource linking or visual correctness.

Raw console output is retained in algorithm-results.txt, junit-results.txt, and sql-results.txt.

## Comparative benchmark

Host: Windows 11 amd64, OpenJDK 64-Bit Server VM 21.0.10, 16 logical processors. Dataset seed 499; unique item IDs/SKUs; quantity and minimum independently uniform from 0 to 99; shuffled input. Twelve warmup runs per approach per size, followed by 21 alternating measured runs. Results are median elapsed milliseconds. A volatile sink consumes each result. JVM warmup, garbage collection, and other machine activity still affect timings.

| Input records n | Candidates k | Sorted ArrayList ms | PriorityQueue drained ms |
|---:|---:|---:|---:|
| 100 | 47 | 0.0226 | 0.0188 |
| 1,000 | 492 | 0.1236 | 0.1372 |
| 10,000 | 4,894 | 0.9282 | 1.0555 |
| 100,000 | 49,509 | 16.2651 | 18.3923 |

The queue was slightly faster in the smallest sample; the list was faster in the other measured sizes. Both approaches have O(n + k log k) time for this full-output use case. These measurements support the simpler list for the current screen, not a universal claim that sorting is always faster. This is an in-memory desktop benchmark, not a phone benchmark, a database benchmark, or a statistically controlled JMH study.

## Android checks completed in the user environment

The user ran the enhanced project in Android Studio and supplied these terminal results:

- `:app:testDebugUnitTest`: **BUILD SUCCESSFUL in 3s**; 22 actionable tasks, 9 executed and 13 up-to-date.
- `:app:connectedDebugAndroidTest`: **BUILD SUCCESSFUL in 37s**; 63 actionable tasks, 38 executed and 25 up-to-date.

These are user-supplied Gradle task results. Detailed XML test reports and per-test counts were not supplied; no specific instrumentation test count is asserted. The project's connected test suite includes RestockDatabaseTest for version-two-to-three preservation/login, target persistence, validation, literal search and restock ordering in temporary databases, plus the inherited package-name test. The deprecation/configuration-cache notices did not prevent successful task completion.

Earlier agent-side build attempts failed with local Windows file-access errors before app compilation. The successful Android Studio runs and observed emulator workflows supersede the earlier pending-build status for this submission. The isolated agent environment itself was not repaired.

## Manual emulator evidence

Screenshots and explicit user confirmations established the following:

- Four original inventory records remained visible with default minimum 5. This verifies visible record continuity, not an independent audit of every database field.
- The mouse at quantity 1 was edited to minimum 10, yielding shortage 9. The user confirmed those values remained after closing and reopening the app.
- Raising its quantity to 10 produced Good status and shortage 0 and removed it from the restock list. The list showed zero matches when no item needed restocking.
- No Target (#36), quantity 0 and minimum 0, appeared as Out with shortage 0. It appeared in Out of Stock but was excluded from Needs Restocking.
- The planned example displayed Adapter (#37, quantity 0/minimum 4), Patch Cable (#38, 3/10), then Spare Switch (#39, 2/3).
- Equal-shortage items were set to quantity 2/minimum 5. Renaming #39 to Aardvark Switch moved it ahead of #38 despite its larger ID, confirming the alphabetical tie-breaker. Giving both the name Patch Cable placed #38 before #39, confirming the final ID tie-breaker. Adapter remained first.
- An oversized minimum displayed 'Minimum stock is too large'; reselecting #39 restored quantity 2/minimum 5. The user confirmed the blank-minimum error, unchanged saved minimum, and clearing of the error on reselection.
- A mistaken edit temporarily set Adapter's SKU to DOES-NOT-EXIST. Its SKU was restored to M3-TEST-A and quantity/minimum to 0/4 before the actual search checks. This was an input-location mistake, not evidence of a search defect.
- DOES-NOT-EXIST in the SKU search box produced zero matches with Needs Restocking active. Show Restock List restored all three candidates in priority order. Searching M3-TEST-S returned only #39.
- All Stock with Quantity Low to High showed quantities 0, 0, 2, 2, 5, 10, 29, 150. Name A to Z showed Adapter, Alienware, Epson, No Target, Patch Cable #38, Patch Cable #39, Razer, SteelSeries.
- Low Stock showed the two Patch Cable records; Good Stock showed the four original items; Out of Stock showed Adapter and No Target. Global counts were 8 total, 2 low and 2 out after the temporary records were added.
- The user explicitly confirmed delete/Cancel behavior, logout/login, and SMS opt-out remaining off after logout/login. No SMS transmission was required by this test procedure.

No claim is made that every possible workflow was tested. Separate manual negative/decimal-input attempts, all search tabs in this milestone, and detailed instrumentation report inspection were not supplied. Automated tests cover additional validation cases. The test records and modified mouse quantity are emulator data, not bundled source-code changes.

## Reproduce

From the enhanced project, with JDK 11 or newer installed:

```powershell
.\verification\run-checks.ps1 -JdkBin 'C:\path\to\jdk\bin'
python .\verification\check_sql.py
```

The first command compiles production algorithm/model/input classes with the supplied standalone harness, executes regression checks, and prints a new benchmark. The second needs Python 3 and only its standard library. Timing results can vary between runs.

With a working Android SDK/Gradle environment:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

The second command requires an emulator or connected test device. Do not confuse source tests being included with their passing on Android.
