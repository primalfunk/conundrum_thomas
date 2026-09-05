import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":thomas:longitudinal"))
    api(project(":thomas:longitudinal-admission"))
    testImplementation(libs.junit)
}
