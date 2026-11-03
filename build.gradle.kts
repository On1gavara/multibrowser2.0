// Файл сборки модуля "app". Лежит по пути app/build.gradle.kts

plugins {
    id("com.android.application")      // превращает код в Android-приложение (APK)
    id("org.jetbrains.kotlin.android") // позволяет писать на Kotlin
}

android {
    // Уникальное имя пакета: должно совпадать со строкой package в MainActivity.kt
    namespace = "com.example.multibrowser"
    compileSdk = 34 // версия Android SDK, против которой компилируем

    defaultConfig {
        applicationId = "com.example.multibrowser" // идентификатор приложения на телефоне
        minSdk = 24    // минимальная версия Android (7.0)
        targetSdk = 34 // версия, под которую приложение "заточено"
        versionCode = 1
        versionName = "1.0"
    }

    // Версия Java, под которую компилируем.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Библиотеки, которые использует приложение.
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")      // удобные расширения для Kotlin
    implementation("androidx.appcompat:appcompat:1.7.0") // AppCompatActivity и совместимость со старыми Android
}
