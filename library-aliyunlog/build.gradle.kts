plugins {
    id("com.android.library")
    kotlin("android")
}

//val libVersion by extra("1.0.16")
val libVersion by extra("1.0.17-SNAPSHOT")

android {
    compileSdkVersion(versions.compileSdk)
    defaultConfig {
        minSdkVersion(versions.minSdk)
        targetSdkVersion(versions.targetSdk)
        consumerProguardFiles("consumer-rules.pro")
    }

    sourceSets.getByName("main").java.srcDirs("src/main/kotlin")

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
    // 阿里云日志
    implementation("com.aliyun.openservices:aliyun-log-android-sdk:2.6.10")
}

// 将library上传到iShow mavenCenter的脚本
//apply("../../public/KeyLibraryMavenCentralUploader.gradle")
// 将library上传到zxsl mavenCenter的脚本
apply("../../public/zxslLibraryMavenUploader.gradle")