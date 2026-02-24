# DrunkGuard API Surface

## C01 ProjectScaffold
Foundation Android project structure. No public API surface yet.

### Application Entry Points
- `DrunkGuardApp` - Hilt application class
- `MainActivity` - Single activity entry point with navigation host

### Project Configuration
- minSdk: 26, targetSdk: 34
- Kotlin: 1.9.20, Gradle: 8.4
- Architecture: MVVM + Clean Architecture with Hilt DI
- Navigation: Single Activity with Navigation Component

## C02 RoomDatabase

### Entities
- `Officer(id, badgeId, name, station, passwordHash, createdAt)`
- `VehicleCheck(id, officerId, timestamp, lat, lng, address, plate, type, color, make, photoPath, intoxicationLevel, confidence, challanId, pdfPath, whatsappSent, isMockData)`
- `AppSettings(key, value)`
- `IntoxicationLevel` enum: SOBER, SLIGHTLY, MODERATELY, HEAVILY

### Repositories (Hilt-injected)
- `OfficerRepository.getCurrentOfficer(): Flow<Officer?>`
- `OfficerRepository.getByBadgeId(badgeId): Officer?`
- `OfficerRepository.saveOfficer(officer): Result<Unit>`
- `VehicleCheckRepository.getAllChecks(): Flow<List<VehicleCheck>>`
- `VehicleCheckRepository.getByOfficer(officerId): Flow<List<VehicleCheck>>`
- `VehicleCheckRepository.getTodayStats(officerId): Pair<Int, Int>` // (total, violations)
- `VehicleCheckRepository.saveCheck(check): Result<Unit>`
- `SettingsRepository.getFineAmounts(): Triple<Int, Int, Int>` // (slightly, moderately, heavily)
- `SettingsRepository.getConfidenceThreshold(): Float`
- `SettingsRepository.isBetaMode(): Boolean`
- `SettingsRepository.isDarkMode(): Boolean`
- `SettingsRepository.setSetting(key, value): Result<Unit>`
- `SettingsRepository.initializeDefaults()`
