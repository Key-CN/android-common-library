object versions {
    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 31

    const val kotlin = "1.6.20"
    const val kotlin_coroutines = "1.6.0"
    const val ktx = "1.7.0"
    const val appcompat = "1.4.1"
    const val annotation = "1.3.0"
    const val lifecycle = "2.4.0"
    const val material = "1.5.0"
    const val constraintlayout = "2.1.3"

    const val okhttp3 = "4.9.3"
}

object deps {
    object kotlin {
        const val gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"

        //const val junit = "org.jetbrains.kotlin:kotlin-test-junit:${versions.kotlin}"
        //const val metadata = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0"

        // 协程
        const val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlin_coroutines}"

        //
        const val coroutines_core_jvm = "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${versions.kotlin_coroutines}"

        // 包含core，自己应该主要是关于Android Handler.
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
        const val annotation = "androidx.annotation:annotation:${versions.annotation}"
        const val constraint_layout = "androidx.constraintlayout:constraintlayout:${versions.constraintlayout}"

        // Lifecycles only (without ViewModel or LiveData)
        const val lifecycle_only = "androidx.lifecycle:lifecycle-runtime-ktx:${versions.lifecycle}"
    }

    object google {
        const val material = "com.google.android.material:material:${versions.material}"
    }

    object square {
        const val okhttp3 = "com.squareup.okhttp3:okhttp:${versions.okhttp3}"
    }
}