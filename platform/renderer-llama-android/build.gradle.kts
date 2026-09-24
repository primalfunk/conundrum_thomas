plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.conundrum.thomas.v2.platform.renderer.llama"
    ndkVersion = "28.2.13676358"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 31
        ndk {
            abiFilters += "arm64-v8a"
        }
        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_PLATFORM=android-31",
                    "-DANDROID_STL=c++_shared",
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(project(":thomas:domain"))
    implementation(project(":thomas:language-renderer"))
}
