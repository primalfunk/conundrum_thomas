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
    testImplementation(libs.junit)
    testImplementation(project(":thomas:domain"))
    testImplementation(project(":thomas:provenance"))
    testImplementation(project(":thomas:engine"))
    testImplementation(project(":thomas:safety"))
    testImplementation(project(":thomas:runtime"))
}

tasks.test {
    systemProperty("thomas.repositoryRoot", rootProject.projectDir.absolutePath)
}
