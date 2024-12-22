# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

