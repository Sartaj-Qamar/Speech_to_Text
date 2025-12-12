plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.codetech.speechtotext"
    compileSdk = 36   // safer + prevents build errors

    defaultConfig {
        applicationId = "com.codetech.speechtotext"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kapt {
    correctErrorTypes = true
}

val room_version = "2.8.4"
val hilt_version = "2.57.2"

dependencies {

    // --- your existing libraries ---

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.volley)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.dexter)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // ML Kit
    implementation(libs.text.recognition)
    implementation(libs.language.id)
    implementation(libs.translate)
    implementation(libs.digital.ink.recognition)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Lottie
    implementation(libs.lottie)

    // Glide
    implementation(libs.glide)

    // Android-image-cropper
    implementation(libs.android.image.cropper)

    // Lifetime
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Room — fixed + using KSP (recommended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt — ONLY one version used
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.dotsindicator.v43)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
