plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.pdfscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pdfscanner"
        minSdk = 24
        targetSdk = 34
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
        compose = true // Enable Jetpack Compose
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.mlkit.document.scanner)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Add ML Kit Document Scanner dependency
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    // Add Coil for image loading with Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.material:material:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Optional: Tooling for debugging in Compose
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.4")
}
