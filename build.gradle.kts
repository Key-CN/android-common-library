// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath(deps.kotlin.gradle_plugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
    }
}

// allprojects是对所有project的配置，包括Root Project。

// subprojects是对所有Child Project的配置
/*subprojects {
    repositories {
        google()
        mavenCentral()
    }
}*/

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}