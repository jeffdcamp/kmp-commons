plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.skie)
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.kover)
    alias(libs.plugins.download)
    id("maven-publish")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {

        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
//        macosX64(),
//        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "KMPCommons"
            binaryOption("bundleId", "org.dbtools.kmp.commons")
            binaryOption("bundleVersion", property("version") as? String ?: "0.0.0")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.datetime)

                implementation(libs.ktor.client.core)
//                implementation(libs.ktor.client.auth)
//                implementation(libs.ktor.client.contentNegotiation)
//                implementation(libs.ktor.client.logging)
//                implementation(libs.ktor.client.resources)
//                implementation(libs.ktor.serialization.json)

                implementation(libs.okio)

                implementation(libs.androidx.datastore.preferences)

                implementation(libs.kermit)

                implementation(libs.touchlab.skie.annotations)
//                implementation(libs.touchlab.stately.concurrency)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
                implementation(libs.assertK)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
    }
}

android {
    namespace = "com.dbtools.kmp.commons"
    compileSdk = LibInfo.AndroidSdk.COMPILE
    defaultConfig {
        minSdk = LibInfo.AndroidSdk.MIN
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    dependencies {
        coreLibraryDesugaring(libs.android.desugar)

        // androidx
        compileOnly(libs.androidx.lifecycle.process)
        compileOnly(libs.androidx.activity)
        compileOnly(libs.androidx.room.common)

        // Firebase
        compileOnly(libs.google.firebase.analytics)
        compileOnly(libs.google.firebase.auth)
        compileOnly(libs.google.firebase.config)
        compileOnly(libs.google.firebase.crashlytics)
        compileOnly(libs.google.firebase.firestore)

    }
}

skie {
    features {
        group("co.touchlab.skie.types") {
            analytics {
                enabled.set(false)
            }
        }
    }
}

// ./gradlew kmp-commons:kmmBridgePublish -PENABLE_PUBLISHING=true
// ./gradlew spmDevBuild
kmmbridge {
//
    mavenPublishArtifacts()
    addGithubPackagesRepository()
    spm()
}

// ./gradlew koverHtmlReport
// ./gradlew koverVerify
koverReport {
    defaults {
        // adds the contents of the reports of `release` Android build variant to default reports
        mergeWith("release")
    }

    verify {
        rule {
            minBound(0)
        }
    }
}

// ./gradlew clean build assembleRelease publishMavenPublicationToMavenLocal publishAndroidReleasePublicationToMavenLocal
// ./gradlew clean build assembleRelease publishMavenPublicationToMavenCentralRepository publishReleasePublicationToMavenCentralRepository
publishing {
    publications {
        create<MavenPublication>("maven") {
            // artifactId defined by module name
            // groupId / version defined in gradle.properties
            from(components["kotlin"])

            pom {
                name.set(Pom.LIBRARY_NAME)
                description.set(Pom.POM_DESCRIPTION)
                url.set(Pom.URL)
                licenses {
                    license {
                        name.set(Pom.LICENCE_NAME)
                        url.set(Pom.LICENCE_URL)
                        distribution.set(Pom.LICENCE_DIST)
                    }
                }
                developers {
                    developer {
                        id.set(Pom.DEVELOPER_ID)
                        name.set(Pom.DEVELOPER_NAME)
                    }
                }
                scm {
                    url.set(Pom.SCM_URL)
                    connection.set(Pom.SCM_CONNECTION)
                    developerConnection.set(Pom.SCM_DEV_CONNECTION)
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                val sonatypeNexusUsername: String? by project
                val sonatypeNexusPassword: String? by project
                username = sonatypeNexusUsername ?: ""
                password = sonatypeNexusPassword ?: ""
            }
        }
    }
}
