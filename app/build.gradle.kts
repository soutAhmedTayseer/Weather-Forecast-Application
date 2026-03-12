plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.weatherforecastapplication"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.weatherforecastapplication"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Retrofit & Gson for Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Room Database for Local Caching
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    // Use ksp instead of kapt if you are using KSP in your project plugins
    kapt("androidx.room:room-compiler:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // WorkManager for Alerts
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Location Services (FusedLocationProvider)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Places API (for the Auto-Complete Search Bar)
    implementation("com.google.android.libraries.places:places:3.3.0")

    // Preferences DataStore for saving Settings
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Required for AppCompatDelegate language switching to work without crashing!
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Add these for GIF support
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")

    // Standard JUnit 4 framework
    testImplementation("junit:junit:4.13.2")

    // MockK: Creates the "fake" classes for testing in isolation
    testImplementation("io.mockk:mockk:1.13.9")

    // Coroutines Testing: Allows you to use runTest and StandardTestDispatcher
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // AndroidX Test core and JUnit runners for the emulator
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    // MockK for Android: Specifically built to run on the emulator
    androidTestImplementation("io.mockk:mockk-android:1.13.9")

    // Coroutines Testing (needed for runBlocking in DAO tests)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Room Testing: Allows you to create the in-memory database
    androidTestImplementation("androidx.room:room-testing:2.6.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}