import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    kotlin("jvm") version "2.0.20"
//    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij.platform") version "2.1.0"
//    id("org.jetbrains.intellij.platform.migration") version "2.1.0"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    // google-java-format
    id("com.github.sherter.google-java-format") version "0.9"
    // license header
    id("com.github.hierynomus.license") version "0.16.1"
    // Sonar support
    id("org.sonarqube") version "5.1.0.4882"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
//        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    }
}

// Configure gradle-intellij-plugin plugin.
intellijPlatform {
    buildSearchableOptions = true
    instrumentCode = false
    projectName = project.name
    pluginConfiguration {
        id = "cookiecode-stepbuilder-plugin"
        name = "Stepbuilder Codegen"
        ideaVersion {
            sinceBuild = "223"
            untilBuild = "243.*" // 243 = 2024.3
        }
        vendor {
            name = "Sebastien Vermeille"
            email = "sebastien.vermeille@gmail.com"
            url = "https://cookiecode.dev"
        }
    }
    pluginVerification {
    }
    publishing {
        host = "https://plugins.jetbrains.com"
        token = System.getenv("PUBLISH_TOKEN")
        channels = listOf("default")
        ideServices = false
        hidden = false
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "sebastienvermeille_intellij-stepbuilder-codegen-plugin")
        property("sonar.organization", "sebastienvermeille")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version = properties("pluginVersion")
    groups = emptyList()
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config.setFrom(files("./detekt-config.yml"))
    buildUponDefaultConfig = true
}

googleJavaFormat {
    toolVersion = "1.1"
}

license {
    header = rootProject.file(".code/LICENSE_HEADER.tpl")
    strictCheck = true
}

tasks {
    // Set the compatibility versions to 17
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    withType<Detekt> {
        jvmTarget = "17"
    }

    withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
        }
    }

    patchPluginXml {
        version = properties("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider { changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML) })
    }
}
