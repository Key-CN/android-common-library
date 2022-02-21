plugins {
    id("com.android.library")
    kotlin("android")
}

val libVersion by extra("1.0.7")

android {
    compileSdkVersion(versions.compileSdk)
    defaultConfig {
        minSdkVersion(versions.minSdk)
        targetSdkVersion(versions.targetSdk)
        consumerProguardFiles("consumer-rules.pro")
    }

    sourceSets.getByName("main").java.srcDirs("src/main/kotlin")

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // androidx
    compileOnly(deps.androidx.appcompat)
    compileOnly(deps.ktx.core)
    // 协程支援一下
    compileOnly(deps.kotlin.coroutines_android)
    compileOnly(deps.androidx.lifecycle_only)
    compileOnly(deps.square.okhttp3)
    // viewBinding需要
    implementation(deps.androidx.annotation)
    // kotlin的反射库
    implementation(kotlin("reflect"))
}

// 将library上传到mavenCenter的脚本
apply("../../public/KeyLibraryMavenCentralUploader.gradle")
