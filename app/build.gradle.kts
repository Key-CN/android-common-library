plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(versions.compileSdk)
    defaultConfig {
        applicationId("io.keyss.library.test")
        minSdkVersion(versions.minSdk)
        targetSdkVersion(versions.targetSdk)
        versionCode(1)
        versionName("1.0.0")
        testInstrumentationRunner("android.support.test.runner.AndroidJUnitRunner")
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
    testImplementation("junit:junit:+")
    androidTestImplementation("androidx.test.ext:junit:+")
    androidTestImplementation("androidx.test.espresso:espresso-core:+")
    implementation(deps.google.material)
    implementation(deps.ktx.core)
    implementation(deps.androidx.appcompat)
    implementation(deps.androidx.constraint_layout)
    implementation(deps.androidx.lifecycle_only)
    implementation(deps.kotlin.coroutines_android)
    implementation(project(":library-common"))
    implementation(project(":library-aliyunlog"))
}