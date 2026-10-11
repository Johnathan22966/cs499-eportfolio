# Nexventory CS 499 Milestone Three

Author: Johnathan Roehsner. Prepared September 27, 2026.

This submission implements the algorithms and data structures enhancement from the Milestone One code review: per-item minimum stock, shortage calculations, deterministic restock priority, stock filters, and sorting choices. The companion Word narrative is submitted separately.

## Contents

- `original/`: preserved CS 360 Project Three source used for the earlier milestone.
- `milestone_two/`: preserved immediate starting point, including the completed software engineering improvements.
- `enhanced/`: Milestone Three Android Studio project, including all prior improvements and the new restocking features.
- `docs/`: implementation notes, complexity analysis, test evidence, revision history, file hashes, and the Android Studio checklist.

Open **enhanced**, not the parent directory, in Android Studio. Let Android Studio resolve the local SDK location. Gradle wrapper files are included. Machine-specific local.properties, build products, and caches are intentionally excluded.

## Verification status

Completed: dependency-free algorithm regression suite (59,321 assertions), six standalone JUnit tests (including one inherited starter test), comparative benchmark, desktop SQLite schema and migration checks, and XML parsing. These results are recorded with their scope in `docs/VERIFICATION.md`.

Android verification completed in the user environment: the user supplied BUILD SUCCESSFUL for :app:testDebugUnitTest (3 seconds) and :app:connectedDebugAndroidTest (37 seconds). Screenshots and user confirmations cover the manual restocking, sorting, filtering, persistence, validation and regression checks listed in `docs/VERIFICATION.md`. Earlier agent-side file-access failures are retained as historical context. Detailed Android test reports were not supplied; per-test instrumentation counts are not asserted.

## Rubric connection

The rubric asks for planned Category Two enhancements and an accompanying narrative explaining the artifact choice and demonstrated skills. `enhanced/app/src/main/java/com/example/nexventory/InventoryAlgorithms.java` contains the collection and comparator work; `verification/` and the test folders provide the evaluation. The Word narrative addresses artifact history, selection and improvements, course outcomes, and reflection. No grade is guaranteed by this package.
