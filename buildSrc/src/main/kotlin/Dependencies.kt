object versions {
    const val compileSdk = 30
    const val buildToolsVersion = "30.0.3"
    const val minSdk = 16
    const val targetSdk = 30

    const val kotlin = "1.5.10"
    const val ktx = "1.5.0"
    const val appcompat = "1.3.0"
}

object deps {
    object kotlin {
        const val gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"

        //const val junit = "org.jetbrains.kotlin:kotlin-test-junit:${versions.kotlin}"
        //const val metadata = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0"
    }

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