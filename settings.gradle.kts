// Этот файл — "точка входа" Gradle (системы сборки). Он говорит:
// где брать плагины, какие версии плагинов использовать и из каких модулей состоит проект.

pluginManagement {
    // Репозитории, откуда Gradle скачивает плагины.
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Версии плагинов: Android-плагин (собирает APK) и Kotlin-плагин (компилирует Kotlin).
    plugins {
        id("com.android.application") version "8.7.3"
        id("org.jetbrains.kotlin.android") version "2.0.21"
    }
}

// Репозитории, откуда скачиваются библиотеки (например, appcompat).
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MultiBrowser"

// В проекте один модуль — "app".
include(":app")
