plugins {



    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") //
    id("org.jetbrains.kotlin.plugin.compose")

}

android {
    namespace = "com.example.composetutorial"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.composetutorial"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // ✅ Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ✅ Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation("androidx.compose.animation:animation:1.5.1") // ✅ Keep only the correct version
    implementation("androidx.compose.material3:material3:1.1.2") // ✅ Keep latest version
    implementation("androidx.navigation:navigation-compose:2.6.0") // ✅ Navigation

    implementation("androidx.compose.material3:material3:1.2.0")

    // ✅ Room Database (No duplicates)
    implementation("androidx.room:room-runtime:2.6.1") // ✅ Keep latest version
    kapt("androidx.room:room-compiler:2.6.1") // ✅ Required for Room annotations
    implementation("androidx.room:room-ktx:2.6.1") // ✅ Room KTX extension

    // ✅ Data Storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ✅ Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.2.2")

    // ✅ Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
