plugins {
    // Plugin para aplicaciones Android
    id("com.android.application")

    // Plugin de Firebase (Google Services)
    // Necesario para usar servicios como Authentication, Firestore, Analytics, etc.
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projects"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projects"
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


    // Habilita características adicionales de compilación
    buildFeatures {
        // Activa View Binding para acceder a las vistas de forma segura (sin findViewById)
        viewBinding = true
    }
}

dependencies {
    // AndroidX básicas
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")

    // Glide para imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}