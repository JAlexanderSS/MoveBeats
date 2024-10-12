plugins {
    alias(libs.plugins.android.application) // Ya aplica el plugin aquí, no necesitas la línea siguiente
    alias(libs.plugins.kotlin.android)
    // Elimina la línea de `id("com.android.application")` para evitar el conflicto
    id("com.google.gms.google-services") // Mantén el plugin de Google Services
}

android {
    namespace = "com.example.movebeats"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.movebeats"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Otras dependencias
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Dependencia directa de MediaSessionCompat
    implementation(libs.androidx.media.v160)

    // Dependencias Firebase
    implementation(libs.firebase.auth.ktx)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Add the dependencies for Firebase products you want to use
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth-ktx")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Dependencias de prueba
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
