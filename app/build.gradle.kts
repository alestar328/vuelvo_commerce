plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    // Crashlytics / Performance: activar cuando añadamos esos SDKs.
    // alias(libs.plugins.firebase.crashlytics)
    // alias(libs.plugins.firebase.perf)
}

android {
    namespace = "com.delta.vuelvo_commerce"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.delta.vuelvo_commerce"
        minSdk = 26
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.billing.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Firebase — el BoM gestiona las versiones de todas las librerías firebase-*
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // Próximos pasos del plan de producción (descomentar al integrarlos):
    // implementation(libs.firebase.auth)
    // implementation(libs.firebase.firestore)
    // implementation(libs.firebase.database)
    // implementation(libs.firebase.storage)
    // implementation(libs.firebase.messaging)
    // implementation(libs.firebase.functions)
    // implementation(libs.firebase.crashlytics)
    // implementation(libs.firebase.config)
    // implementation(libs.firebase.perf)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}