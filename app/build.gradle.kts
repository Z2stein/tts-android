import com.android.build.gradle.AppExtension

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
    }
}

apply(plugin = "com.android.application")

configure<AppExtension> {
    namespace = "com.ttsandroid.app"
    compileSdkVersion(35)

    defaultConfig {
        applicationId = "com.ttsandroid.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    add("implementation", project(":domain"))
    add("implementation", project(":platform"))
}
