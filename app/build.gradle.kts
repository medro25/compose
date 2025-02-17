plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.composetutorial"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.composetutorial"
        minSdk = 24
        targetSdk = 33
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
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ✅ Updated Compose BOM and UI versions
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui:1.4.3") // Changed from 1.5.0 (which doesn't exist yet)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // ✅ WorkManager for background notifications
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // ✅ Coil for image loading
    implementation("io.coil-kt:coil-compose:2.2.2")

    // ✅ Data storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ✅ Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.compose.compiler:compiler:1.5.3")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

}
