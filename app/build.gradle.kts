plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cidev.inventorymanage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cidev.inventorymanage"
        minSdk = 24
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

    // SOAP client — talks to the existing InvManageWebService (Service.asmx)
    // exactly like the legacy Windows Mobile app does. No server changes needed.
    //
    // NOTE: com.google.code.ksoap2-android:ksoap2-android:3.6.4 (the
    // "official" coordinates) only live on an old Sonatype OSSRH repo that
    // isn't mirrored to Maven Central — Gradle can't resolve it from there,
    // which is what broke the first Codemagic build attempt. Using an
    // actively-mirrored JitPack fork instead: same library, same API.
    implementation("com.github.kekru:ksoap2-android:v3.6.2-useragentfix")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
