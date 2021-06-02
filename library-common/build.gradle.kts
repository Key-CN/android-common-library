plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = versions.compileSdk
    buildToolsVersion = versions.buildToolsVersion

    sourceSets.getByName("main") {
        java.srcDir("src/main/kotlin")
    }

    defaultConfig {
        minSdk = versions.minSdk
        targetSdk = versions.targetSdk
        versionCode = 1
        versionName = "1.0.0"
        consumerProguardFiles("consumer-rules.pro")
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
    //
}

// 将library上传到mavenCenter的脚本
apply("../../public/KeyLibraryMavenCentralUploader.gradle")
