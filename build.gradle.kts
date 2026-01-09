import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11

val user: String by project
val repo: String by project
val g: String by project
val artifact: String by project
val v: String by project
val desc: String by project

plugins {
    kotlin("plugin.compose") version libs.versions.kotlin
    kotlin("multiplatform") version libs.versions.kotlin
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.compose)
    alias(libs.plugins.dokka)
}

group = g
version = v

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm()

    androidLibrary {
        namespace = "$g.$artifact"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget.set(JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        nodejs()
    }

    @Suppress("unused")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.material3)
                api(libs.malefic.theming)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                api(libs.precompose)
            }
        }
    }
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

tasks.apply {
    build {
        dependsOn(named("formatAndLintKotlin"))
        dependsOn(dokkaGenerate)
    }
    publish {
        dependsOn(named("formatAndLintKotlin"))
    }
    register("formatAndLintKotlin") {
        group = "formatting"
        description = "Fix Kotlin code style deviations with kotlinter"
        dependsOn(named("formatKotlin"))
        dependsOn(named("lintKotlin"))
    }
    check {
        dependsOn("installKotlinterPrePushHook")
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }
    pluginsConfiguration.html {
        footerMessage.set("&copy; 2025 Om Gupta &lt;ogupta4242@gmail.com&gt;")
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(g, artifact, v)

    pom {
        name = repo
        description = desc
        inceptionYear = "2025"
        url = "https://github.com/$user/$repo"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit.malefic.xyz"
            }
        }
        developers {
            developer {
                name = "Om Gupta"
                email = "ogupta4242@gmail.com"
                url = "malefic.xyz"
            }
        }
        scm {
            url = "https://github.com/$user/$repo"
            connection = "scm:git:git://github.com/$user/$repo.git"
            developerConnection = "scm:git:ssh://github.com/$user/$repo.git"
        }
    }
}
