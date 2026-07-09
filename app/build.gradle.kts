plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cidev.inventorymanage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cidev.inventorymanage"
        minSdk = 24          // Android 7+ covers virtually all modern rugged handhelds
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-scaffold"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // SOAP client — talks to the existing InvManageWebService (Service.asmx) exactly
    // like the legacy Windows Mobile app does. No server changes needed.
    //
    // NOTE: the "official" com.google.code.ksoap2-android:ksoap2-android:3.6.4
    // coordinate is NOT on Maven Central — it only ever lived on a legacy
    // Sonatype OSSRH repo that's no longer reliable. Using a maintained
    // fork published via JitPack instead (JitPack repo is already declared
    // in settings.gradle.kts).
    implementation("com.github.kekru:ksoap2-android:v3.6.2-useragentfix")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
