plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // alias(libs.plugins.google.services)        // Decommentare quando si aggiunge google-services.json
    // alias(libs.plugins.firebase.crashlytics)   // Decommentare insieme a google-services
}

android {
    namespace   = "com.paolomarchionetti.savio"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.paolomarchionetti.savio"
        minSdk          = 26          // Android 8.0 — copre ~95% dei device attivi
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Aggiornamento automatico dei JSON seed senza nuovo APK
        buildConfigField("String", "DATA_BASE_URL",
            "\"https://raw.githubusercontent.com/paolomarchionetti/savio-data/main/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled     = true
            isShrinkResources   = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable        = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose BOM — gestisce le versioni di tutte le librerie Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt — Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Room — database locale
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore — preferenze utente
    implementation(libs.datastore.preferences)

    // Retrofit + OkHttp — aggiornamento JSON remoto
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx)
    implementation(libs.okhttp.logging)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil — caricamento immagini
    implementation(libs.coil.compose)

    // Firebase (commentato finché non serve)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.crashlytics)
    // implementation(libs.firebase.analytics)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
