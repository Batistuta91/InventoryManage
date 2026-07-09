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

    // SOAP client — hand-rolled (see network/SoapClient.kt) using OkHttp +
    // Android's built-in XmlPullParser, instead of the ksoap2-android
    // library. Tried ksoap2-android first (via a JitPack fork, since the
    // official Maven coordinates don't resolve); that build then failed
    // with a real, verified error: ksoap2-android's own POM depends on
    // net.sourceforge.kxml:kxml:2.2.4, net.sourceforge.kobjects, and
    // net.sourceforge.me4se — none of which are published to any
    // currently-reachable Maven repository. A raw SOAP 1.1 POST + XML
    // parse avoids that dependency chain entirely.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
