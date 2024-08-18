enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "aqueue"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

includeBuild("build-logic")

include("core", "example")
