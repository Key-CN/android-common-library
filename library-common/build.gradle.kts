plugins {
    id("com.android.library")
    kotlin("android")
}

val libVersion by extra("1.0.17")

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
    // 只是为了image数据转换的工具类
    compileOnly("androidx.camera:camera-core:1.1.0-beta01")
    // viewBinding需要
    implementation(deps.androidx.annotation)
    // kotlin的反射库
    implementation(kotlin("reflect"))
    // 转换工具类，实际还是依赖项目中使用的Gson
    compileOnly("com.google.code.gson:gson:2.9.0")
}

// 将library上传到iShow mavenCenter的脚本
//apply("../../public/KeyLibraryMavenCentralUploader.gradle")
// 将library上传到zxsl mavenCenter的脚本
apply("../../public/zxslLibraryMavenUploader.gradle")