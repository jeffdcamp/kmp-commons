@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
//    alias(libs.plugins.kover)
    alias(libs.plugins.download)
    id("maven-publish")
    signing
}

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.set(
            listOf(
                "-opt-in=kotlin.uuid.ExperimentalUuidApi",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
        )
    }

    androidLibrary {
        namespace = "com.dbtools.kmp.commons"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_17
                )
            }
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    linuxX64()

//    js {
//        browser()
//        nodejs()
//    }

    // Mac / iOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "KMPCommons"
            binaryOption("bundleId", "org.dbtools.kmp.commons")
            binaryOption("bundleVersion", property("version") as? String ?: "0.0.0")
        }
    }

    // ==== currently unsupported ====
//    macosArm64()
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//    watchosArm32()
//    watchosArm64()
//    watchosSimulatorArm64()
//    watchosDeviceArm64()
//    watchosX64()
//    tvosArm64()
//    tvosSimulatorArm64()
//    tvosX64()

//    mingwX64()
//    linuxArm64()

//    androidNativeArm32()
//    androidNativeArm64()
//    androidNativeX86()
//    androidNativeX64()


    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.okio)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.kermit)
//                implementation(libs.touchlab.skie.annotations)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.coroutines.test)
                implementation(libs.assertK)

                implementation(libs.kotlin.coroutines.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.client.resources)

                implementation(libs.okio)
            }
        }
    }
}

// ./gradlew koverHtmlReport
// ./gradlew koverVerify
//kover {
//    reports {
//        verify {
//            rule {
//                minBound(0)
//            }
//        }
//    }
//}

// ./gradlew clean build detekt publishToMavenLocal
// ./gradlew clean build detekt publishAllPublicationsToMavenCentralRepository
fun MavenPublication.mavenCentralPom() {
    pom {
        name.set("Kmp Commons")
        description.set("Kmp Commons")
        url.set("https://github.com/jeffdcamp/kmp-commons")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("jcampbell")
                name.set("Jeff Campbell")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/jeffdcamp/kmp-commons.git")
            developerConnection.set("scm:git:git@github.com:jeffdcamp/kmp-commons.git")
            url.set("https://github.com/jeffdcamp/kmp-commons")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            // artifactId defined by module name
            // groupId / version defined in gradle.properties
            from(components["kotlin"])

            if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                // already has publications, just need to add javadoc task
                val javadocJar by tasks.creating(Jar::class) {
                    from("javadoc")
                    archiveClassifier.set("javadoc")
                }
                publications.all {
                    if (this is MavenPublication) {
                        artifact(javadocJar)
                        mavenCentralPom()
                    }
                }
                // create task to publish all apple (macos, ios, tvos, watchos) artifacts
                val publishApple by tasks.registering {
                    publications.all {
                        if (name.contains(Regex("macos|ios|tvos|watchos"))) {
                            val publicationNameForTask = name.replaceFirstChar(Char::uppercase)
                            dependsOn("publish${publicationNameForTask}PublicationToMavenCentralRepository")
                        }
                    }
                }
            } else {
                // Need to create source, javadoc & publication
                val java = extensions.getByType<JavaPluginExtension>()
                java.withSourcesJar()
                java.withJavadocJar()
                publications {
                    create<MavenPublication>("lib") {
                        from(components["java"])
                        mavenCentralPom()
                    }
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

signing {
    setRequired {
        findProperty("signing.keyId") != null
    }

    publishing.publications.all {
        sign(this)
    }
}

// TODO: remove after following issues are fixed
// https://github.com/gradle/gradle/issues/26091
// https://youtrack.jetbrains.com/issue/KT-46466
tasks {
    withType<PublishToMavenRepository> {
        dependsOn(withType<Sign>())
    }

    named("compileTestKotlinIosArm64") {
        dependsOn(named("signIosArm64Publication"))
    }
    named("compileTestKotlinIosSimulatorArm64") {
        dependsOn(named("signIosSimulatorArm64Publication"))
    }
    named("compileTestKotlinIosX64") {
        dependsOn(named("signIosX64Publication"))
    }
    named("compileTestKotlinLinuxX64") {
        dependsOn(named("signLinuxX64Publication"))
    }
    named("compileTestKotlinMacosArm64") {
        dependsOn(named("signMacosArm64Publication"))
    }
    named("compileTestKotlinMacosX64") {
        dependsOn(named("signMacosX64Publication"))
    }
}
