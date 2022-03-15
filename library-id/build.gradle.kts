plugins {
    id("com.android.library")
    kotlin("android")
}

val libVersion by extra("1.0.1")

android {
    compileSdkVersion(versions.compileSdk)
    defaultConfig {
        minSdkVersion(versions.minSdk)
        targetSdkVersion(versions.targetSdk)
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(files("libs/liantian.jar"))
}

// 将library上传到iShow mavenCenter的脚本
//apply("../../public/KeyLibraryMavenCentralUploader.gradle")
// 将library上传到zxsl mavenCenter的脚本
apply("../../public/zxslLibraryMavenUploader.gradle")