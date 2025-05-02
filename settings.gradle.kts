pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.0.0"
        id("org.jetbrains.kotlin.android") version "1.8.10"
        id("com.google.gms.google-services") version "4.3.15"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // ou REPOSITORIES_MODE.PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Be9ik_Wallet"
include(":app")
