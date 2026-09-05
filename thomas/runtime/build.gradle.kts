import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":thomas:domain"))
    api(project(":thomas:provenance"))
    api(project(":thomas:engine"))
    api(project(":thomas:safety"))
    api(project(":thomas:personal-data-persistence"))
    api(project(":thomas:language-evidence"))
    api(project(":thomas:journal"))
    api(project(":thomas:biographer"))
    api(project(":thomas:retrieval"))
    api(project(":thomas:context-packet"))
    api(project(":thomas:therapy-longitudinal"))
    api(project(":thomas:language-renderer"))
}
