plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.protpetverso_1"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.protpetverso_1"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Volley para requisições HTTP/JSON
    implementation("com.android.volley:volley:1.2.1")
    // EncryptedSharedPreferences para armazenamento seguro do Token
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    androidTestImplementation(libs.ext.junit)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.material:material:1.11.0")
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.14.0")
}