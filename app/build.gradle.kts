plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

}

android {
    namespace = "com.codetech.speechtotext"
    compileSdk = 36

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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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


// Define versions for dependencies
val camerax_version = "1.2.2"
val nav_version = "2.7.0"
val google_truth_version = "1.1.3"
val room_version = "2.5.2"
val retrofit_version = "2.9.0"
val lottieVersion = "6.1.0"
val hilt_version = "2.47"

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.androidx.foundation.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.dexter)
    implementation(libs.androidx.viewpager2)

    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    implementation(libs.text.recognition)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // ML-Kit to identify the language of the recognized text
    implementation(libs.language.id)

    // Lottie Animation
    implementation(libs.lottie)

    // Retrofit for networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Dagger Hilt dependencies
    implementation(libs.hilt.android)

    //koin for dependency injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)

    // ML-Kit for text translation
    implementation(libs.translate)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.circleindicator)

    implementation(libs.digital.ink.recognition)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.android.image.cropper)


    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler.v250)

    implementation(libs.hilt.android.v2511)
    kapt(libs.hilt.android.compiler)

    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.dotsindicator)


}







