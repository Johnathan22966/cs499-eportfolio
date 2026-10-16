"""Explain the replacement for the historical SQLite-only smoke check."""
print("The database is now accessed through Room. Run gradlew.bat :app:testDebugUnitTest")
print("RoomDatabaseJvmTest exercises migrations, persistence, duplicate rejection, and rollback.")
print("For device checks run gradlew.bat :app:connectedDebugAndroidTest with an emulator connected.")
