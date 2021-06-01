plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = Dependencies.versions.compileSdk
    buildToolsVersion = Dependencies.versions.buildToolsVersion

    defaultConfig {
        applicationId = "io.keyss.library.test"
        minSdk = Dependencies.versions.minSdk
        targetSdk = Dependencies.versions.targetSdk
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
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
    implementation(Dependencies.deps.google.material)
    implementation(Dependencies.deps.ktx.core)
    implementation(Dependencies.deps.androidx.appcompat)
    implementation(Dependencies.deps.androidx.constraintlayout)
}