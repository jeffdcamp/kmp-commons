# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.9.0] - 2026-10-07

### Added
- `DirectDownloadRequest.maxAttempts` / `retryDelay` — `DirectDownloader` can now retry a failed download.
  Defaults to `1` attempt (no retry), so existing callers are unchanged; opt in per request. A download is
  not resumable, so every retry starts over from byte 0 — keep the count small and prefer a generous
  `HttpTimeout.socketTimeoutMillis` for a connection that is merely slow. Only *transient* failures are
  retried (no HTTP response, 5xx, 408, 429); a 404, 401/403 or 3xx is taken as the server's final answer,
  and a local failure (`LOCAL_ERROR_CODE`) returns immediately. A `cancel()` during `retryDelay` stops the
  retry without spending another request.
- `DirectDownloadResult.NO_RESPONSE_CODE` — names the `code = -1` sentinel for "failed without an HTTP
  response".
- `DirectDownloadResult.LOCAL_ERROR_CODE` — `code = -2`, a local failure that a retry cannot change (an
  unusable target path, a target directory that cannot be created, or an existing target file with
  `overwriteExisting == false`). Never retried.
- Unit tests for `DirectDownloader` retry behavior (`ktor-client-mock` + Okio `FakeFileSystem`)
- `NetworkUtil.close()` releases platform resources (cancels the Apple `NWPathMonitor`, which previously leaked);
  a no-op on Android, JVM and Linux. Call it when finished with a `NetworkUtil`.
- JVM `NetworkUtil` connectivity probe host/port are configurable (`NetworkUtil(probeHost, probePort)`), for
  networks that block the default `dns.google:53`.
- Unit tests for `FileSystem.unzip()` (Zip Slip and symbolic link handling)

### Security
- `FileSystem.unzip()` (Okio): fixed Zip Slip (path traversal). An entry that resolves outside `targetDir`
  (e.g. `../../shared_prefs/auth.xml`), or that would be written through a symbolic link, now fails the extraction.
- Ktor error messages and logs (`executeSafely()`, `executeSafelyCached()`, `saveBodyToFile()`) no longer include
  the URL query string or user info, which may contain tokens or PII.

### Changed
- Raised Android minSdk to 26
- `NetworkUtil` (Apple): the `NWPathMonitor` callback uses a non-blocking `trySend` so it never blocks the main
  dispatch queue.
- `NetworkUtil` (JVM, Linux): `connectionInfoFlow()` probes off the collector's dispatcher (`Dispatchers.IO` on
  JVM, `Dispatchers.Default` on Linux).
- Upgraded Gradle Wrapper to 9.8.0, AGP to 9.4.1, Kotlin to 2.4.20
- Upgraded Ktor to 3.6.0, Okio to 3.18.2, Kermit to 2.2.0, Kover to 0.9.11, AndroidX Core KTX to 1.19.1, and
  versions plugin to 0.64.0

### Fixed
- `executeSafely()` / `executeSafelyCached()` rethrow `CancellationException` (and no longer catch `Error`s)
  instead of returning a failure, so cancelled callers stay cancelled.
- `Instant.nextDayOfWeek()`, `nextOrSameDayOfWeek()`, `previousDayOfWeek()` and `previousOrSameDayOfWeek()` are
  no longer off by an hour across a DST change.
- `NetworkUtil.isConnected()` (Linux): the probe `connect()` is now bounded by a 3 second timeout (it could hang
  for the OS default on an offline/firewalled network).
- Documentation: `ApiResponse` / `CacheApiResponse` `Failure.Error.Forbidden` is a 403 response (was mislabeled 401).
- `DirectDownloader.inProgress` was never reset, so an instance could only ever perform one download. It is
  now released when the download ends, and an instance can be reused.
- `DirectDownloader.cancelRequested` was never reset either, so an instance that had been canceled once
  reported "Download canceled" for every later download (after still spending the request). A `cancel()` now
  applies only to the download it interrupted.
- `cancel()` is now written through an atomic, like `inProgress`. It is called from a different thread than
  the download loop that reads it, so as a plain `var` the flag could be missed or read stale.

## [1.8.0] - 2026-08-21

### Added
- Added Apple (iOS/macOS) klib cross-compilation support so all targets can be assembled/published from a Linux CI host (framework linking still requires macOS)
- Added Gradle Daemon JVM toolchain configuration and the foojay-resolver-convention plugin
- Enabled Kover coverage verification and host-side (JVM) unit tests for androidMain code

### Changed
- Migrated to the `android { }` Kotlin Multiplatform DSL and only declare Apple frameworks on macOS hosts
- Moved Detekt and download-config setup into the module build script
- Raised Android minSdk to 24
- Switched the versions plugin id from `com.github.ben-manes` to `io.github.ben-manes`
- Upgraded Gradle Wrapper to 9.7.0, AGP to 9.3.1, Kotlin to 2.4.10
- Upgraded Ktor to 3.5.2, Okio to 3.18.1, Kover to 0.9.9, Detekt to 2.0.0-alpha.6, and versions plugin to 0.61.0

### Fixed
- Narrowed NetworkUtil host reachability check to catch `IOException` instead of all exceptions

## [1.7.0] - 2026-06-24

### Added
- Added AnalyticEvent, AnalyticScreen, and AnalyticError data classes
- Added support for Chinese script variants (`zh-Hans`, `zh-Hant`) in language code conversions
- Added optional JSON file logging support to TestStrategy using Okio

### Changed
- Refactored AppAnalytics and Strategy interfaces to use the new data classes for logEvent(), logScreen(), and logError()
- Updated LanguageCodeUtil.toLanguageCodeIso3() to evaluate full BCP 47 language tags before falling back to the primary language
- Renamed `ISO_639_3_TO_ISO_639_1` to `ISO_639_3_TO_BCP47` and updated reverse mapping logic to prefer more specific codes
- Upgraded Gradle Wrapper to 9.6.0, Kotlin to 2.4.0, AGP to 9.2.1
- Upgraded Android compileSdk to 37
- Upgraded Ktor to 3.5.0
- Upgraded kotlinx libraries (coroutines 1.11.0, serialization 1.11.0, datetime 0.8.0)
- Upgraded coreKtx, detekt, vanniktechPlugin, and versionsPlugin

### Fixed
- Fixed property assignment in AppAnalytics.setLogLevel()

## [1.6.0] - 2026-04-08

### Added
- Added KMP network connectivity utilities (NetworkUtil, ConnectionInfo)
- Added KMP language code conversion utilities (LanguageCode)

### Changed
- Removed iosX64 and macosX64 targets
- Updated Kotlin to 2.3.20, AGP to 9.1.0, Ktor to 3.4.2
- Updated Kermit to 2.1.0, Okio to 3.17.0, Kover to 0.9.8
- Updated AndroidX DataStore to 1.2.1, Core KTX to 1.18.0

## [1.5.1] - 2026-02-18

### Added
- Added String.toUri() extension function

## [1.5.0] - 2026-02-04

### Added
- Added Uri class

### Changed
- Updated Gradle to 9.3.1, AGP to 9.0.0, and Kotlin to 2.3.0
- Migrated Detekt to 2.0.0-alpha.2 and updated task configuration
- Refactored Android compiler options
- Minor code cleanups

## [1.4.1] - 2025-12-23

### Changed
- Rollback Kotlin to 2.2.21 (better support different KMP libraries (Example: SKIE))

## [1.4.0] - 2025-12-15

### Added
- Added DataValueClassSerializer for Instant, LocalDate, LocalTime, and LocalDateTime

### Changed
- Updated versions

## [1.3.0] - 2025-11-15

### Added
- Added .run profiles
- Added LanguageCode

### Changed
- Changed publishing to use Vanniktech Plugin
- Updated versions

### Removed
- Removed deprecated InstantIso8601Serializer

## [1.2.1] - 2025-07-11

### Added
- Added KotlinTimeSerializer

### Changed
- Kotlin Date-Time 1.7.1
- Added improvements to KotlinDateTimeExt
- Updated versions

## [1.2.0] - 2025-06-30

### Changed

- Kotlin 2.2.0
- Kotlin Date-Time 1.7.0 (changed kotlinx.datetime.Instant to kotlin.time.Instant)
- Updated versions

## [1.1.4] - 2025-05-29

### Changed

- Updated versions

## [1.1.3] - 2025-04-12

### Changed

- Changed Atomicfu to Kotlin 2.1.20 Standard Library: Common atomic types
- Changed Android Gradle Library Plugin to "com.android.kotlin.multiplatform.library"

## [1.1.2] - 2025-04-10

### Changed

- Kotlin 2.1.20
- Ktor 3.1.2
- Kotlin Serialization 1.8.1
- Kotlin DateTime 0.6.2
- Kotlin Atomicfu 0.27.0
- Okio 3.11.0
- Datastore 1.1.4
- Gradle 8.11.1


## [1.1.1] - 2024-12-08

### Added

- Added KotlinDateTimeExt: weeksUntil, changed extensions that use 'Clock.System' to pass it in as a property
- Added check for httpResponse success on DirectDownloader

## [1.1.0] - 2024-12-08

### Added

- Added Instant.isToday(), DayOfWeek.plus(), DayOfWeek.minus() to KotlinDateTimeExt

### Changed

- Kotlin 2.1.0
- Ktor 3.0.2
- Kermit 2.0.5
- AGP 8.7.3
- Gradle 8.11.1

## [1.0.0] - 2024-11-16

### Added

- Added more extension functions for kotlin-datetime

### Changed

- Changed JVM target to JDK 21
- Fixed issue with executeSafelyCached
- Updated versions

## [0.0.3] - 2024-09-30

### Added

- Added DataValueClassSerializer classes to simplify serialization of a 'data class' acting as a 'value class'

### Changed

- Updated versions

### Removed

- Removed Android specific dependency (focus this library on pure KMP common code)
- Removed SavedStateHandleExt.kt (Use Typesafe navigation instead)
- Removed JvmMapExt.kt (Use kotlin-datetime instead)

## [0.0.2] - 2024-09-30

### Added

- Added support for jvm

### Changed

- Updated versions

### Removed

- Removed Android specific dependency (focus this library on pure KMP common code)
- Removed SavedStateHandleExt.kt (Use Typesafe navigation instead)
- Removed JvmMapExt.kt (Use kotlin-datetime instead)

## [0.0.1] - 2024-03-30

### Added

- Initial commit (copied/converted to kmp project from https://github.com/jeffdcamp/android-commons)

