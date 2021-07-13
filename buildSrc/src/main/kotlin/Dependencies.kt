object versions {
    const val compileSdk = 30
    const val buildTools = "30.0.3"
    const val minSdk = 16
    const val targetSdk = 30

    const val kotlin = "1.5.20"
    const val kotlin_coroutines = "1.5.0"
    const val ktx = "1.6.0"
    const val appcompat = "1.3.0"
}

object deps {
    object kotlin {
        const val gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"

        //const val junit = "org.jetbrains.kotlin:kotlin-test-junit:${versions.kotlin}"
        //const val metadata = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0"

        // 协程
        const val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlin_coroutines}"
        // 包含core
        const val coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.kotlin_coroutines}"
    }

    /**
     * Android KTX 是包含在 Android Jetpack 及其他 Android 库中的一组 Kotlin 扩展程序。
     * KTX 扩展程序可以为 Jetpack、Android 平台及其他 API 提供简洁的惯用 Kotlin 代码。
     */
    object ktx {
        const val core = "androidx.core:core-ktx:${versions.ktx}"
    }

    object androidx {
        const val appcompat = "androidx.appcompat:appcompat:${versions.appcompat}"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    }

    object google {
        const val material = "com.google.android.material:material:1.3.0"
    }
}