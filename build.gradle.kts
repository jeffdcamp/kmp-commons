import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.vanniktechPublishing) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.download)
    alias(libs.plugins.versions) // ./gradlew dependencyUpdates -Drevision=release --refresh-dependencies
}

// ===== Gradle Dependency Check =====
// ./gradlew dependencyUpdates -Drevision=release
// ./gradlew dependencyUpdates -Drevision=release --refresh-dependencies
//
// ./gradlew app:dependencyInsight --configuration debugRuntimeClasspath --dependency androidx.room
// ./gradlew shared:dependencyInsight --configuration commonMainApiDependenciesMetadata --dependency androidx.room
// ./gradlew shared:resolvableConfigurations | grep "^Configuration"
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(
            version = candidate.version,
            includeStablePreRelease = true
        )
    }
}

fun isNonStable(version: String, includeStablePreRelease: Boolean): Boolean {
    val stablePreReleaseKeyword = listOf("RC", "BETA").any { version.uppercase().contains(it) }
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+$".toRegex()
    val isStable = if (includeStablePreRelease) {
        stableKeyword || regex.matches(version) || stablePreReleaseKeyword
    } else {
        stableKeyword || regex.matches(version)
    }
    return isStable.not()
}

allprojects {
    // ===== Detekt =====
    // Known KMP issues https://github.com/detekt/detekt/issues/5611
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId).also {
        // download detekt config file
        tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadDetektConfig") {
            download {
                onlyIf { !file("$projectDir/build/config/detektConfig.yml").exists() }
                src("https://raw.githubusercontent.com/jeffdcamp/kmp-commons/master/detekt/detektConfig-latest.yml")
                dest("$projectDir/build/config/detektConfig.yml")
            }
        }

        // make sure when running detekt, the config file is downloaded
        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            // Target version of the generated JVM bytecode. It is used for type resolution.
            this.jvmTarget = "17"
            dependsOn("downloadDetektConfig")
        }

        detekt {
            allRules = true // fail build on any finding
            buildUponDefaultConfig = true // preconfigure defaults
            config.setFrom(files("$projectDir/build/config/detektConfig.yml")) // point to your custom config defining rules to run, overwriting default behavior
        }

        tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
            setSource(files(project.projectDir))
            exclude("**/build/**")
            exclude("**/*.kts")


            exclude {
                it.file.relativeTo(projectDir).startsWith(buildDir.relativeTo(projectDir))
            }
        }

        tasks.register("detektAll") {
            dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
        }
    }
}
