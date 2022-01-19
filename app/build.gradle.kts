plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = versions.compileSdk
    defaultConfig {
        applicationId = "io.keyss.library.test"
        minSdk = versions.minSdk
        targetSdk = versions.targetSdk
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation(deps.google.material)
    implementation(deps.ktx.core)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.constraint_layout)
    implementation(deps.androidx.lifecycle_only)
    implementation(deps.kotlin.coroutines_android)
    implementation(project(":library-common"))
}