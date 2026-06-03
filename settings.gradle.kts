// Machine-local JDK path (see jdk.local.properties.example)
val jdkLocal = file("jdk.local.properties")
if (jdkLocal.exists()) {
    jdkLocal.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .forEach { line ->
            val key = line.substringBefore("=").trim()
            val value = line.substringAfter("=").trim()
            if (key == "org.gradle.java.home" && value.isNotEmpty()) {
                System.setProperty(key, value)
            }
        }
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QuranDaily"
include(":app")
include(":core")
