# Kmp-Commons
Kotlin Multiplatform shared/common code library for Android/iOS/Desktop apps and libraries

## Features

Helpers for

* Analytics
* Datastore
* Function extensions for
    * Context
    * Enum
    * Flow
    * Map
    * SavedStateHandle
* Firebase
    * Firestore
    * Remote Config
* Logging
* Networking

## Publishing Locally
Edit `kmp-commons/gradle.properties` and add a fourth portion to the version (X.X.X.X)

#### Android
```shell
./gradlew clean build assembleRelease publishMavenPublicationToMavenLocal publishAndroidReleasePublicationToMavenLocal
```

#### iOS
```shell
./gradlew clean spmDevBuild
```

## Publish Release Build
* Edit `kmp-commons/gradle.properties` and set the version
* Edit `CHANGELOG.md` set the version and date, update the `Unreleased` link and add the version link
* Commit these files with the message `Prep Release <version>`

Run Tests
```shell
./gradlew clean tRUT iSAT
```

#### Publish iOS
```shell
./gradlew kmp-commons:kmmBridgePublish -PENABLE_PUBLISHING=true
```
* Commit `Package.swift` with message `<version>`
* Tag commit `<version>`
* Push commit and tag to repo
* Create release on Github

#### Publish Android
```shell
./gradlew clean build assembleRelease publishMavenPublicationToMavenCentralRepository publishReleasePublicationToMavenCentralRepository
```
License
=======

    Copyright 2012-2024 Jeff Campbell

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.