plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.conundrum.thomas.v2.platform.speech"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":thomas:domain"))
    testImplementation(libs.junit)
}
