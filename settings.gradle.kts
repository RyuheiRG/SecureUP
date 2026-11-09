pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                excludeGroup("com.google.devtools.ksp")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                // KSP se publica también en Maven Central. Evitar que Gradle lo
                // pida a dl.google.com: si ese host no resuelve, la descarga falla
                // en vez de continuar con el siguiente repositorio.
                excludeGroup("com.google.devtools.ksp")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "Secure UP"
include(":app")
