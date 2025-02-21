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
    kotlin("jvm") version "2.1.10"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij.platform") version "2.2.1"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    // google-java-format
    id("com.github.sherter.google-java-format") version "0.9"
    // license header
    id("com.github.hierynomus.license") version "0.16.1"
    // Sonar support
    id("org.sonarqube") version "6.0.1.5171"
    // plugin verifier
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
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    }
    runtimeOnly("org.jetbrains.intellij.plugins:verifier-cli:1.381")
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
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
        vendor {
            name = "Sebastien Vermeille"
            email = "sebastien.vermeille@gmail.com"
            url = "https://cookiecode.dev"
        }
    }
    pluginVerification {
        cliPath = file("build/libs/verifier-cli-1.379.jar")

        ides {
            recommended()
//            select {
//                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
//                channels = listOf(ProductRelease.Channel.RELEASE)
//                sinceBuild = properties("pluginSinceBuild")
//                untilBuild = properties("pluginUntilBuild")
//            }
        }
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
        sourceCompatibility = properties("targetJdk")
        targetCompatibility = properties("targetJdk")
    }
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    withType<Detekt> {
        jvmTarget = properties("targetJdk")
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
            }.joinToString("\n").run {
                markdownToHTML(this)
            },
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider { changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML) })
    }
}

tasks.register<Copy>("downloadVerifierCli") {
    val outputDir = layout.buildDirectory.dir("libs").get().asFile

    from(
        configurations.create("verifierCli").apply {
            dependencies.add(
                project.dependencies.create("org.jetbrains.intellij.plugins:verifier-cli:1.381"),
            )
        },
    )

    into(outputDir)

    doLast {
        println("Dependency downloaded to: ${outputDir.absolutePath}")
    }
}
