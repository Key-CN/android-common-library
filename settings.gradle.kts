dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
/*pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}*/
rootProject.name = "android-common-library"
include(
    ":app",
    ":library-aliyunlog",
)
include(":library-common")
