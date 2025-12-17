# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

